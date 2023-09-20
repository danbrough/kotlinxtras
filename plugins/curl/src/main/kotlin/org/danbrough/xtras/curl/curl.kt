package org.danbrough.xtras.curl

import org.danbrough.xtras.XtrasDSLMarker
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.library.XtrasLibrary
import org.danbrough.xtras.library.xtrasCreateLibrary
import org.danbrough.xtras.library.xtrasRegisterSourceTask
import org.danbrough.xtras.log
import org.danbrough.xtras.source.gitSource
import org.gradle.api.Plugin
import org.gradle.api.Project

object Curl {
  const val extensionName = "curl"
  const val sourceURL = "https://github.com/curl/curl.git"
  const val defaultVersion = "8.3.0"
  const val defaultCommit = "curl-8_3_0"
}


class CurlPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.log("CURL PLUGIN: apply()")
  }
}

@XtrasDSLMarker
fun Project.xtrasCurl(
  ssl: XtrasLibrary,
  name: String = Curl.extensionName,
  curlVersion: String = properties.getOrDefault("$name.version", Curl.defaultVersion).toString(),
  curlCommit: String = properties.getOrDefault("$name.commit", Curl.defaultCommit).toString(),
  configure: XtrasLibrary.() -> Unit = {},
) = xtrasCreateLibrary(name, curlVersion, ssl) {
  gitSource(Curl.sourceURL, curlCommit)
  configure()

  supportedTargets.forEach { target ->
    val prepareSourceTask = xtrasRegisterSourceTask(XtrasLibrary.TaskName.PREPARE_SOURCE, target) {
      outputs.file("configure")
      dependsOn(extractSourceTaskName(target))
      commandLine(buildEnvironment.binaries.autoreconf, "-fi")
    }


    val configureTask = xtrasRegisterSourceTask(XtrasLibrary.TaskName.CONFIGURE, target) {
      dependsOn(libraryDeps.map { it.extractArchiveTaskName(target) })
      dependsOn(prepareSourceTask)
      outputs.file(workingDir.resolve("Makefile"))

      val configureOptions = mutableListOf(
        "./configure",
        "--host=${target.hostTriplet}",
        "--with-openssl=${ssl.libsDir(target)}",
        //"--with-ca-path=/etc/ssl/certs:/etc/security/cacerts:/etc/ca-certificates",
        "--with-ca-bundle=/etc/ssl/certs/ca-certificates.crt",
        "--prefix=${buildDir(target)}",
        "--disable-ntlm",
        "--disable-ldap",
        "--disable-ldaps",
        "--without-zlib",
      )

      commandLine(configureOptions)
    }


    xtrasRegisterSourceTask(XtrasLibrary.TaskName.BUILD, target) {
      dependsOn(configureTask)
      outputs.dir(buildDir(target))
      commandLine("make", "install")
    }
  }

  cinterops {
    interopsPackage = "libcurl"
    headers = """
      headers = curl/curl.h
      #linkerOpts = -lwolfssl -lcurl -lz 
      linkerOpts =  -lz -lssl -lcrypto -lcurl
      #staticLibraries.linux = libcurl.a
      #staticLibraries.android = libcurl.a
      
      """.trimIndent()
  }
}

/*

System types:
  --build=BUILD     configure for building on BUILD [guessed]
  --host=HOST       cross-compile to build programs to run on HOST [BUILD]

Optional Features:
  --disable-option-checking  ignore unrecognized --enable/--with options
  --disable-FEATURE       do not include FEATURE (same as --enable-FEATURE=no)
  --enable-FEATURE[=ARG]  include FEATURE [ARG=yes]
  --enable-maintainer-mode
                          enable make rules and dependencies not useful (and
                          sometimes confusing) to the casual installer
  --enable-silent-rules   less verbose build output (undo: "make V=1")
  --disable-silent-rules  verbose build output (undo: "make V=0")
  --enable-debug          Enable debug build options
  --disable-debug         Disable debug build options
  --enable-optimize       Enable compiler optimizations
  --disable-optimize      Disable compiler optimizations
  --enable-warnings       Enable strict compiler warnings
  --disable-warnings      Disable strict compiler warnings
  --enable-werror         Enable compiler warnings as errors
  --disable-werror        Disable compiler warnings as errors
  --enable-curldebug      Enable curl debug memory tracking
  --disable-curldebug     Disable curl debug memory tracking
  --enable-symbol-hiding  Enable hiding of library internal symbols
  --disable-symbol-hiding Disable hiding of library internal symbols
  --enable-ares[=PATH]    Enable c-ares for DNS lookups
  --disable-ares          Disable c-ares for DNS lookups
  --disable-rt            disable dependency on -lrt
  --enable-ech            Enable ECH support
  --disable-ech           Disable ECH support
  --enable-code-coverage  Provide code coverage
  --enable-dependency-tracking
                          do not reject slow dependency extractors
  --disable-dependency-tracking
                          speeds up one-time build
  --disable-largefile     omit support for large files
  --enable-shared[=PKGS]  build shared libraries [default=yes]
  --enable-static[=PKGS]  build static libraries [default=yes]
  --enable-fast-install[=PKGS]
                          optimize for fast installation [default=yes]
  --disable-libtool-lock  avoid locking (might break parallel builds)
  --enable-http           Enable HTTP support
  --disable-http          Disable HTTP support
  --enable-ftp            Enable FTP support
  --disable-ftp           Disable FTP support
  --enable-file           Enable FILE support
  --disable-file          Disable FILE support
  --enable-ldap           Enable LDAP support
  --disable-ldap          Disable LDAP support
  --enable-ldaps          Enable LDAPS support
  --disable-ldaps         Disable LDAPS support
  --enable-rtsp           Enable RTSP support
  --disable-rtsp          Disable RTSP support
  --enable-proxy          Enable proxy support
  --disable-proxy         Disable proxy support
  --enable-dict           Enable DICT support
  --disable-dict          Disable DICT support
  --enable-telnet         Enable TELNET support
  --disable-telnet        Disable TELNET support
  --enable-tftp           Enable TFTP support
  --disable-tftp          Disable TFTP support
  --enable-pop3           Enable POP3 support
  --disable-pop3          Disable POP3 support
  --enable-imap           Enable IMAP support
  --disable-imap          Disable IMAP support
  --enable-smb            Enable SMB/CIFS support
  --disable-smb           Disable SMB/CIFS support
  --enable-smtp           Enable SMTP support
  --disable-smtp          Disable SMTP support
  --enable-gopher         Enable Gopher support
  --disable-gopher        Disable Gopher support
  --enable-mqtt           Enable MQTT support
  --disable-mqtt          Disable MQTT support
  --enable-manual         Enable built-in manual
  --disable-manual        Disable built-in manual
  --enable-libcurl-option Enable --libcurl C code generation support
  --disable-libcurl-option
                          Disable --libcurl C code generation support
  --enable-libgcc         use libgcc when linking
  --enable-ipv6           Enable IPv6 (with IPv4) support
  --disable-ipv6          Disable IPv6 support
  --enable-openssl-auto-load-config
                          Enable automatic loading of OpenSSL configuration
  --disable-openssl-auto-load-config
                          Disable automatic loading of OpenSSL configuration
  --enable-versioned-symbols
                          Enable versioned symbols in shared library
  --disable-versioned-symbols
                          Disable versioned symbols in shared library
  --enable-threaded-resolver
                          Enable threaded resolver
  --disable-threaded-resolver
                          Disable threaded resolver
  --enable-pthreads       Enable POSIX threads (default for threaded resolver)
  --disable-pthreads      Disable POSIX threads
  --enable-verbose        Enable verbose strings
  --disable-verbose       Disable verbose strings
  --enable-sspi           Enable SSPI
  --disable-sspi          Disable SSPI
  --enable-crypto-auth    Enable cryptographic authentication
  --disable-crypto-auth   Disable cryptographic authentication
  --enable-ntlm           Enable NTLM support
  --disable-ntlm          Disable NTLM support
  --enable-ntlm-wb[=FILE] Enable NTLM delegation to winbind's ntlm_auth
                          helper, where FILE is ntlm_auth's absolute filename
                          (default: /usr/bin/ntlm_auth)
  --disable-ntlm-wb       Disable NTLM delegation to winbind's ntlm_auth
                          helper
  --enable-tls-srp        Enable TLS-SRP authentication
  --disable-tls-srp       Disable TLS-SRP authentication
  --enable-unix-sockets   Enable Unix domain sockets
  --disable-unix-sockets  Disable Unix domain sockets
  --enable-cookies        Enable cookies support
  --disable-cookies       Disable cookies support
  --enable-socketpair     Enable socketpair support
  --disable-socketpair    Disable socketpair support
  --enable-http-auth      Enable HTTP authentication support
  --disable-http-auth     Disable HTTP authentication support
  --enable-doh            Enable DoH support
  --disable-doh           Disable DoH support
  --enable-mime           Enable mime API support
  --disable-mime          Disable mime API support
  --enable-dateparse      Enable date parsing
  --disable-dateparse     Disable date parsing
  --enable-netrc          Enable netrc parsing
  --disable-netrc         Disable netrc parsing
  --enable-progress-meter Enable progress-meter
  --disable-progress-meter
                          Disable progress-meter
  --enable-dnsshuffle     Enable DNS shuffling
  --disable-dnsshuffle    Disable DNS shuffling
  --enable-get-easy-options
                          Enable curl_easy_options
  --disable-get-easy-options
                          Disable curl_easy_options
  --enable-alt-svc        Enable alt-svc support
  --disable-alt-svc       Disable alt-svc support
  --enable-headers-api    Enable headers-api support
  --disable-headers-api   Disable headers-api support
  --enable-hsts           Enable HSTS support
  --disable-hsts          Disable HSTS support
  --enable-websockets     Enable WebSockets support
  --disable-websockets    Disable WebSockets support

Optional Packages:
  --with-PACKAGE[=ARG]    use PACKAGE [ARG=yes]
  --without-PACKAGE       do not use PACKAGE (same as --with-PACKAGE=no)
  --with-schannel         enable Windows native SSL/TLS
  --with-secure-transport enable Apple OS native SSL/TLS
  --with-amissl           enable Amiga native SSL/TLS (AmiSSL)
  --with-ssl=PATH         old version of --with-openssl
  --without-ssl           build without any TLS library
  --with-openssl=PATH     Where to look for OpenSSL, PATH points to the SSL
                          installation (default: /usr/local/ssl); when
                          possible, set the PKG_CONFIG_PATH environment
                          variable instead of using this option
  --with-gnutls=PATH      where to look for GnuTLS, PATH points to the
                          installation root
  --with-mbedtls=PATH     where to look for mbedTLS, PATH points to the
                          installation root
  --with-wolfssl=PATH     where to look for WolfSSL, PATH points to the
                          installation root (default: system lib default)
  --with-bearssl=PATH     where to look for BearSSL, PATH points to the
                          installation root
  --with-rustls=PATH      where to look for rustls, PATH points to the
                          installation root
  --with-nss-deprecated   confirm you realize NSS is going away
  --with-nss=PATH         where to look for NSS, PATH points to the
                          installation root
  --with-test-nghttpx=PATH
                          where to find nghttpx for testing
  --with-test-caddy=PATH  where to find caddy for testing
  --with-test-httpd=PATH  where to find httpd/apache2 for testing

  --with-pic[=PKGS]       try to use only PIC/non-PIC objects [default=use
                          both]
  --with-aix-soname=aix|svr4|both
                          shared library versioning (aka "SONAME") variant to
                          provide on AIX, [default=aix].
  --with-gnu-ld           assume the C compiler uses GNU ld [default=no]
  --with-sysroot[=DIR]    Search for dependent libraries within DIR (or the
                          compiler's sysroot if not specified).
  --with-mingw1-deprecated
                          confirm you realize support for mingw v1 is dying
  --with-hyper=PATH       Enable hyper usage
  --without-hyper         Disable hyper usage
  --with-zlib=PATH        search for zlib in PATH
  --without-zlib          disable use of zlib
  --with-brotli=PATH      Where to look for brotli, PATH points to the BROTLI
                          installation; when possible, set the PKG_CONFIG_PATH
                          environment variable instead of using this option
  --without-brotli        disable BROTLI
  --with-zstd=PATH        Where to look for libzstd, PATH points to the
                          libzstd installation; when possible, set the
                          PKG_CONFIG_PATH environment variable instead of
                          using this option
  --without-zstd          disable libzstd
  --with-ldap-lib=libname Specify name of ldap lib file
  --with-lber-lib=libname Specify name of lber lib file
  --with-gssapi-includes=DIR
                          Specify location of GSS-API headers
  --with-gssapi-libs=DIR  Specify location of GSS-API libs
  --with-gssapi=DIR       Where to look for GSS-API
  --with-default-ssl-backend=NAME
                          Use NAME as default SSL backend
  --without-default-ssl-backend
                          Use implicit default SSL backend
  --with-egd-socket=FILE  Entropy Gathering Daemon socket pathname
  --with-random=FILE      read randomness from FILE (default=/dev/urandom)
  --with-ca-bundle=FILE   Path to a file containing CA certificates (example:
                          /etc/ca-bundle.crt)
  --without-ca-bundle     Don't use a default CA bundle
  --with-ca-path=DIRECTORY
                          Path to a directory containing CA certificates
                          stored individually, with their filenames in a hash
                          format. This option can be used with the OpenSSL,
                          GnuTLS and mbedTLS backends. Refer to OpenSSL
                          c_rehash for details. (example: /etc/certificates)
  --without-ca-path       Don't use a default CA path
  --with-ca-fallback      Use the built in CA store of the SSL library
  --without-ca-fallback   Don't use the built in CA store of the SSL library
  --without-libpsl        disable support for libpsl cookie checking
  --without-libgsasl      disable libgsasl support for SCRAM
  --with-libssh2=PATH     Where to look for libssh2, PATH points to the
                          libssh2 installation; when possible, set the
                          PKG_CONFIG_PATH environment variable instead of
                          using this option
  --with-libssh2          enable libssh2
  --with-libssh=PATH      Where to look for libssh, PATH points to the libssh
                          installation; when possible, set the PKG_CONFIG_PATH
                          environment variable instead of using this option
  --with-libssh           enable libssh
  --with-wolfssh=PATH     Where to look for wolfssh, PATH points to the
                          wolfSSH installation; when possible, set the
                          PKG_CONFIG_PATH environment variable instead of
                          using this option
  --with-wolfssh          enable wolfssh
  --with-librtmp=PATH     Where to look for librtmp, PATH points to the
                          LIBRTMP installation; when possible, set the
                          PKG_CONFIG_PATH environment variable instead of
                          using this option
  --without-librtmp       disable LIBRTMP
  --with-winidn=PATH      enable Windows native IDN
  --without-winidn        disable Windows native IDN
  --with-libidn2=PATH     Enable libidn2 usage
  --without-libidn2       Disable libidn2 usage
  --with-nghttp2=PATH     Enable nghttp2 usage
  --without-nghttp2       Disable nghttp2 usage
  --with-ngtcp2=PATH      Enable ngtcp2 usage
  --without-ngtcp2        Disable ngtcp2 usage
  --with-nghttp3=PATH     Enable nghttp3 usage
  --without-nghttp3       Disable nghttp3 usage
  --with-quiche=PATH      Enable quiche usage
  --without-quiche        Disable quiche usage
  --with-msh3=PATH        Enable msh3 usage
  --without-msh3          Disable msh3 usage
  --with-zsh-functions-dir=PATH
                          Install zsh completions to PATH
  --without-zsh-functions-dir
                          Do not install zsh completions
  --with-fish-functions-dir=PATH
                          Install fish completions to PATH
  --without-fish-functions-dir
                          Do not install fish completions

Some influential environment variables:
  CC          C compiler command
  CFLAGS      C compiler flags
  LDFLAGS     linker flags, e.g. -L<lib dir> if you have libraries in a
              nonstandard directory <lib dir>
  LIBS        libraries to pass to the linker, e.g. -l<library>
  CPPFLAGS    (Objective) C/C++ preprocessor flags, e.g. -I<include dir> if
              you have headers in a nonstandard directory <include dir>
  CPP         C preprocessor
  LT_SYS_LIBRARY_PATH
              User-defined run-time library search path.

Use these variables to override the choices made by `configure' or to help
it to find libraries and programs with nonstandard names/locations.

Report bugs to <a suitable curl mailing list: https://curl.se/mail/>.
*/
