package demo.ssh2

import demo.log
import io.ktor.util.encodeBase64
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.cValue
import kotlinx.cinterop.convert
import kotlinx.cinterop.cstr
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.useContents
import libssh2.LIBSSH2_HOSTKEY_TYPE_ECDSA_256
import libssh2.LIBSSH2_INVALID_SOCKET
import libssh2.LIBSSH2_SESSION
import libssh2.SSH_DISCONNECT_BY_APPLICATION
import libssh2.libssh2_exit
import libssh2.libssh2_hostkey_hash
import libssh2.libssh2_init
import libssh2.libssh2_session_disconnect_ex
import libssh2.libssh2_session_free
import libssh2.libssh2_session_handshake
import libssh2.libssh2_session_init_ex
import libssh2.libssh2_session_last_errno
import libssh2.libssh2_trace
import libssh2.libssh2_userauth_list
import libssh2.libssh2_userauth_password_ex
import libssh2.libssh2_userauth_publickey_fromfile_ex
import platform.linux.inet_addr
import platform.posix.AF_INET
import platform.posix.SOCK_STREAM
import platform.posix.close
import platform.posix.connect
import platform.posix.errno
import platform.posix.getenv
import platform.posix.shutdown
import platform.posix.sockaddr_in
import platform.posix.socket
import platform.posix.strerror


/*
static const char *pubkey = ".ssh/id_rsa.pub";
static const char *privkey = ".ssh/id_rsa";
static const char *username = "dan";*/



data class Config(
  var username: String = "username",
  var password: String = "",
  var pubKey: String = ".ssh/id_rsa.pub",
  var privKey: String = ".ssh/id_rsa",
  var sshPort: Int = 22,
  var sshHost: String = "127.0.0.1",
)

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
fun main(args: Array<String>) {

  val config = Config()

  if (args.isNotEmpty()) {
    config.sshHost = args[0]
  }

  if (args.size > 1){
    config.username = args[1]
  }

  if (args.size > 2){
    config.password = args[2]
  }

  log.info("running ssh2 demo: $config")


  var sock = LIBSSH2_INVALID_SOCKET
  var session: CPointer<LIBSSH2_SESSION>? = null

  memScoped {


    runCatching {
      libssh2_init(0).also {
        if (it != 0) error("libssh2 initialization failed $it")
      }

      log.trace("libssh2 initialized")

      sock = socket(AF_INET, SOCK_STREAM, 0)

      if (sock == LIBSSH2_INVALID_SOCKET) {
        error("failed to create socket");
      }
      log.trace("created socket")


      val sockAddr = cValue<sockaddr_in> {
        sin_family = AF_INET.convert()
        sin_port = ((config.sshPort shr 8) or ((config.sshPort and 0xff) shl 8)).convert()
        sin_addr.s_addr = inet_addr(config.sshHost)
      }

      log.debug("connecting to ${config.sshHost}:${config.sshPort}")

      sockAddr.useContents {
        connect(sock, ptr.reinterpret(), sizeOf<sockaddr_in>().convert()).also {
          log.info("connect returned $it")
          if (it != 0) {
            error("error: ${strerror(errno)?.toKString()}")
          }
        }
      }

      session = libssh2_session_init_ex(null, null, null, null)
        ?: error("Failed to initialize ssh session")

      libssh2_trace(session, 0)

      libssh2_session_handshake(session, sock).also {
        if (it != 0) error("Failure establishing SSH session: $it ${strerror(errno)?.toKString()}")
      }

      libssh2_hostkey_hash(session, LIBSSH2_HOSTKEY_TYPE_ECDSA_256)?.also { data ->
        (0 until 32).map { data[it] }.toByteArray().encodeBase64().trim('=').also {
          log.debug("fingerprint: $it")
        }
      }


      /*
       /* check what authentication methods are available */
    userauthlist = libssh2_userauth_list(session, username,
                                         (unsigned int)strlen(username));
       */


      var authPw = 0

      libssh2_userauth_list(session, config.username, config.username.length.convert()).also {
        if (it == null) error("libssh2_userauth_list ${config.username} failed")
        val authMethods = it.toKString().split(",")

        log.info("auth methods: $authMethods")
        if (authMethods.contains("password")) {
          log.trace("supports password authentication")
          authPw = authPw.or(1)
        }
        if (authMethods.contains("keyboard-interactive")) {
          log.trace("supports keyboard-interactive authentication")
          authPw = authPw.or(2)
        }
        if (authMethods.contains("publickey")) {
          log.trace("supports public key authentication")
          authPw = authPw.or(4)
        }

        log.trace("auth_pw: $authPw")

        if (args.contains("-p")) {
          authPw = 1
          log.trace("auth_pw forced to: $authPw")
        } else if (args.contains("-i")) {
          authPw = 2
          log.trace("auth_pw forced to: $authPw")
        } else if (args.contains("-k")) {
          authPw = 4
          log.trace("auth_pw forced to: $authPw")
        }

      }

      when(authPw){
        1 -> {
          log.debug("attempting password authentication with username: ${config.username} password<${config.password}> to ${config.sshHost}:${config.sshPort}")
          libssh2_userauth_password_ex(session,config.username,config.username.cstr.size.convert(),config.password,config.password.cstr.size.convert(),null).also {
            if (it != 0) error(strerror(it)?.toKString() ?: "null")
          }
        }
        2->error("interactive auth not supported")
        4 -> {
          val home = getenv("HOME")!!.toKString()
          val pubKey = "$home/.ssh/id_rsa.pub"
          val privKey = "$home/.ssh/id_rsa"
          log.trace("loading keys from $pubKey $privKey")
           libssh2_userauth_publickey_fromfile_ex(session,config.username,config.username.length.convert(),pubKey,privKey,null).also {
             if (it != 0)
             error("libssh2_userauth_publickey_fromfile_ex returned $it")
           }
        }
        else -> error("invalid authPw: $authPw")
      }


    }.exceptionOrNull().also {

      if (it != null) log.error(it.message, it)

      if (sock != LIBSSH2_INVALID_SOCKET) {
        log.info("closing sock")
        shutdown(sock, 2);
        close(sock)
        /*     #ifdef WIN32
                 closesocket(sock);
             #else
             close(sock);
             #endif*/
      }


      if (session != null) {
        log.debug("disconnecting session")
        libssh2_session_disconnect_ex(session, SSH_DISCONNECT_BY_APPLICATION, "Normal Shutdown", "")
        log.debug("freeing session")
        libssh2_session_free(session);
      }



      log.info("finished")
      libssh2_exit()
    }
  }


}