package demo.ssh2

import demo.log
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
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
import kotlinx.cinterop.usePinned
import libssh2.LIBSSH2_CHANNEL
import libssh2.LIBSSH2_CHANNEL_PACKET_DEFAULT
import libssh2.LIBSSH2_CHANNEL_WINDOW_DEFAULT
import libssh2.LIBSSH2_HOSTKEY_TYPE_ECDSA_256
import libssh2.LIBSSH2_INVALID_SOCKET
import libssh2.LIBSSH2_SESSION
import libssh2.SSH_DISCONNECT_BY_APPLICATION
import libssh2.libssh2_channel_close
import libssh2.libssh2_channel_eof
import libssh2.libssh2_channel_free
import libssh2.libssh2_channel_open_ex
import libssh2.libssh2_channel_process_startup
import libssh2.libssh2_channel_read_ex
import libssh2.libssh2_exit
import libssh2.libssh2_hostkey_hash
import libssh2.libssh2_init
import libssh2.libssh2_session_disconnect_ex
import libssh2.libssh2_session_free
import libssh2.libssh2_session_handshake
import libssh2.libssh2_session_init_ex
import libssh2.libssh2_session_last_errno
import libssh2.libssh2_session_last_error
import libssh2.libssh2_trace
import libssh2.libssh2_userauth_list
import libssh2.libssh2_userauth_password_ex
import libssh2.libssh2_userauth_publickey_fromfile_ex
import org.danbrough.utils.io.toBase64
import platform.linux.free
import platform.linux.inet_addr
import platform.posix.AF_INET
import platform.posix.SOCK_STREAM
import platform.posix.close
import platform.posix.connect
import platform.posix.errno
import platform.posix.fflush
import platform.posix.fwrite
import platform.posix.getenv
import platform.posix.shutdown
import platform.posix.sockaddr_in
import platform.posix.socket
import platform.posix.stdout
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

  if (args.size > 1) {
    config.username = args[1]
  }

  if (args.size > 2) {
    config.password = args[2]
  }

  val command = if (args.size > 4) args[4] else "date"

  log.info("running ssh2 demo: $config")


  var sock = LIBSSH2_INVALID_SOCKET
  var session: CPointer<LIBSSH2_SESSION>? = null
  var channel: CPointer<LIBSSH2_CHANNEL>? = null

  memScoped {

    inline fun errorCheck(
      err: Int = libssh2_session_last_errno(session),
      msg: String? = null
    ): Nothing = throw let {
      val p = cValue<CPointerVar<ByteVar>>().ptr
      libssh2_session_last_error(session, p, null, 1)
      val errMessage = p[0]?.toKString()
      //log.info("error message:<$errMessage>")
      if (p[0] != null) {
        free(p[0])
      }
      IllegalStateException("${msg ?: "Error:$err"}: $errMessage")
    }



    runCatching {
      libssh2_init(0).also {
        if (it != 0) error("libssh2 initialization failed $it")
      }

      log.trace("libssh2 initialized")

      sock = socket(AF_INET, SOCK_STREAM, 0)

      if (sock == LIBSSH2_INVALID_SOCKET) {
        error("failed to create socket as sock == LIBSSH2_INVALID_SOCKET")
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


      session = libssh2_session_init_ex(null, null, null, null) ?: errorCheck()

      libssh2_trace(session, 0)

      libssh2_session_handshake(session, sock).also {
        if (it != 0)
          errorCheck(msg = "Handshake failed")
      }

      libssh2_hostkey_hash(session, LIBSSH2_HOSTKEY_TYPE_ECDSA_256)?.also { data ->
        (0 until 32).map { data[it] }.toByteArray().toBase64().trim('=').also {
          log.debug("fingerprint: $it")
        }
      }


      /*
       /* check what authentication methods are available */
    userauthlist = libssh2_userauth_list(session, username,
                                         (unsigned int)strlen(username));
       */


      var authPw = 0

      libssh2_userauth_list(session, config.username, config.username.cstr.size.convert()).also {
        if (it == null) errorCheck(msg = "libssh2_userauth_list ${config.username} failed")
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

        if (args.size > 3) {
          when (args[3]) {
            "-p" -> authPw = 1
            "-i" -> authPw = 2
            "-k" -> authPw = 4
            else -> error("auth should be -i,-k or -p")

          }
          log.trace("auth_pw forced to: $authPw")
        }
      }

      when (authPw) {
        1 -> {
          log.debug("attempting password authentication with username: ${config.username} password<${config.password}> to ${config.sshHost}:${config.sshPort}")

          libssh2_userauth_password_ex(
            session,
            config.username,
            config.username.cstr.size.convert(),
            config.password,
            config.password.cstr.size.convert(),
            null
          ).also {
            if (it != 0) {
              errorCheck(it, "libssh2_userauth_password_ex: ${config.username} failed.")
            }
          }
        }

        2 -> error("interactive auth not supported")
        4 -> {
          val home = getenv("HOME")!!.toKString()
          val pubKey = "$home/.ssh/id_rsa.pub"
          val privKey = "$home/.ssh/id_rsa"
          log.trace("attempting publickey auth from $pubKey $privKey")

          libssh2_userauth_publickey_fromfile_ex(
            session,
            config.username,
            config.username.length.convert(),
            pubKey,
            privKey,
            config.password
          ).also {
            if (it != 0)
              errorCheck(it, "libssh2_userauth_publickey_fromfile_ex failed")
          }
        }

        else -> error("invalid authPw: $authPw")
      }

      /*
          libssh2_channel_open_ex((session), "session", sizeof("session") - 1, \
                                  LIBSSH2_CHANNEL_WINDOW_DEFAULT, \
                                  LIBSSH2_CHANNEL_PACKET_DEFAULT, NULL, 0)

       */

      log.debug("calling libssh2_channel_open_ex")
      channel = libssh2_channel_open_ex(
        session, "session", 8.toUInt(), LIBSSH2_CHANNEL_WINDOW_DEFAULT.toUInt(),
        LIBSSH2_CHANNEL_PACKET_DEFAULT.toUInt(), null, 0.toUInt()
      )

      if (channel == null)
        errorCheck(msg = "libssh2_channel_open_ex failed")


      log.debug("running command: <$command> over ssh")
      /*
      #define libssh2_channel_exec(channel, command) \
    libssh2_channel_process_startup((channel), "exec", sizeof("exec") - 1, \
                                    (command), (unsigned int)strlen(command))

       */
      val request = "exec"
      libssh2_channel_process_startup(
        channel,
        request,
        (request.cstr.size - 1).convert(),
        command,
        command.length.convert()
      ).also {
        if (it != 0) errorCheck(it, "libssh2_channel_process_startup failed")
      }

      /*
      #define libssh2_channel_read(channel, buf, buflen) \
    libssh2_channel_read_ex((channel), 0, \
                            (buf), (buflen))

       */


      val bufSize = 1024
      ByteArray(bufSize).usePinned { pinned ->
        val p = pinned.addressOf(0)
        while (libssh2_channel_eof(channel) == 0) {
          libssh2_channel_read_ex(channel, 0, p, bufSize.convert()).also { read ->
            if (read < 0) {
              log.warn("unable to read response: $read")
              errorCheck(read.toInt(), "Unable to read response")
            }
            fwrite(p, 1.convert(), read.convert(), stdout)
            fflush(stdout)
          }
        }
        log.debug("finished reading")
      }


    }.exceptionOrNull().also {

      if (it != null) log.error(it.message, it)


      if (libssh2_channel_close(channel) != 0) {
        log.error("Unable to close channel")
      }

      if (channel != null) {
        log.debug("freeing channel")
        libssh2_channel_free(channel)
      }

      if (sock != LIBSSH2_INVALID_SOCKET) {
        log.info("closing sock")
        shutdown(sock, 2)
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
        libssh2_session_free(session)
      }



      log.info("finished")
      libssh2_exit()
    }
  }


}