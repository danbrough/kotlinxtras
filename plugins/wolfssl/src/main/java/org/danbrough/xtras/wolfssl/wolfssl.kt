package org.danbrough.xtras.wolfssl

import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.XtrasDSLMarker
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.library.XtrasLibrary
import org.danbrough.xtras.library.xtrasCreateLibrary
import org.danbrough.xtras.library.xtrasRegisterSourceTask
import org.danbrough.xtras.log
import org.danbrough.xtras.source.gitSource
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

const val WOLFSSL_VERSION = "5.6.3"
const val WOLFSSL_COMMIT = "v5.6.3-stable"

object WolfSSL {
  const val extensionName = "wolfSSL"
  const val sourceURL = "https://github.com/wolfSSL/wolfssl.git"
}

class WolfSSLPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.log("WolfSSLPlugin.apply()")
    target.afterEvaluate {
      target.log("WolfSSLPlugin.afterEvaluate")
    }
  }
}


@XtrasDSLMarker
fun Project.xtrasWolfSSL(
  name: String = WolfSSL.extensionName,
  version: String = properties.getOrDefault("wolfssl.version", WOLFSSL_VERSION).toString(),
  commit: String = properties.getOrDefault("wolfssl.commit", WOLFSSL_COMMIT).toString(),
  configure: XtrasLibrary.() -> Unit = {},
) = xtrasCreateLibrary(name, version) {
  publishingGroup = XTRAS_PACKAGE
  gitSource(WolfSSL.sourceURL, commit)
  cinterops {
    headers = """
          #staticLibraries =  libcrypto.a libssl.a
          headers =  wolfssl/ssl.h wolfssl/openssl/ssl.h wolfssl/openssl/err.h wolfssl/openssl/bio.h wolfssl/openssl/evp.h
          linkerOpts.linux = -ldl -lc -lm -lwolfssl  -lpthread
          linkerOpts.android = -ldl -lc -lm -lwolfssl  -lpthread
          linkerOpts.macos = -ldl -lc -lm -lwolfssl  -lpthread
          linkerOpts.mingw = -lm -lwolfssl  -lpthread -c
          compilerOpts.android = -D__ANDROID_API__=21
          #compilerOpts =  -Wno-macro-redefined -Wno-deprecated-declarations  -Wno-incompatible-pointer-types-discards-qualifiers
          #compilerOpts = -static
          """.trimIndent()
  }

  configure()

  supportedTargets.forEach { target ->


    val prepareSourceTask = xtrasRegisterSourceTask(XtrasLibrary.TaskName.PREPARE_SOURCE, target) {
      if (HostManager.hostIsMingw) commandLine(
        buildEnvironment.binaries.bash, "-c", "./autogen.sh"
      )
      else commandLine("./autogen.sh")
      outputs.file(workingDir.resolve("configure"))
    }


    val configureSourceTask = xtrasRegisterSourceTask(XtrasLibrary.TaskName.CONFIGURE, target) {
      dependsOn(prepareSourceTask)
      outputs.file(workingDir.resolve("Makefile"))
      val configureOptions = mutableListOf(
        "./configure",
        "--host=${target.hostTriplet}",
        "--prefix=${buildDir(target).absolutePath.replace('\\', '/')}",
//      "--disable-fasthugemath",
//      "--disable-bump",
//      "--enable-fortress",
//      "--disable-debug",
//      "--disable-ntru",
        "--disable-examples",
        //  "--enable-distro",
//      "--enable-reproducible-build",
        "--enable-curve25519",
        "--enable-ed25519",
        "--enable-curve448",
        "--enable-static",
        "--enable-des3",
        "--enable-ed448",
        "--enable-sha512",
        "--with-max-rsa-bits=8192",
        "--enable-altcertchains",
        "--enable-certreq",//        Enable cert request generation (default: disabled)
        "--enable-certext",//        Enable cert request extensions (default: disabled)
        //  --enable-certgencache   Enable decoded cert caching (default: disabled)
        //--enable-altcertchains  Enable using alternative certificate chains, only
        //   require leaf certificate to validate to trust root
        //--enable-testcert       Enable Test Cert (default: disabled)
        "--enable-certservice",
        "--enable-altcertchains",
//      "--enable-writedup",
        "--disable-crypttests",
        "--disable-crypttests-libs",

        "--enable-opensslextra",
        //"--enable-openssh",
        //"--enable-libssh2",
        "--enable-keygen",
        "--enable-certgen",
        "--enable-ssh",
        "--enable-wolfssh",
        "--disable-examples",
        "--enable-postauth",

        )

      when (target) {
        KonanTarget.ANDROID_ARM32, KonanTarget.ANDROID_ARM64, KonanTarget.ANDROID_X86, KonanTarget.ANDROID_X64 -> {
          configureOptions.add("--enable-singlethreaded")
        }

        KonanTarget.LINUX_ARM32_HFP -> {
          // configureOptions.add("--disable-asm")
        }

        KonanTarget.MACOS_ARM64, KonanTarget.IOS_ARM64 -> {
          configureOptions.add("--disable-asm")
        }

        else -> {}
      }
//      val configureOptions2 = mutableListOf(
//        "./configure",
//        "--host=${target.hostTriplet}",
//        "--prefix=${buildDir(target)}",
//        "--enable-all",
//        "--disable-crl-monitor",
//      )
      if (HostManager.hostIsMingw) commandLine(
        buildEnvironment.binaries.bash, "-c", configureOptions.joinToString(" ")
      )
      else commandLine(configureOptions)


    }


    println("REGISTERED CONFIGURE TASK: $configureSourceTask")


    xtrasRegisterSourceTask(XtrasLibrary.TaskName.BUILD, target) {
      dependsOn(configureSourceTask)
      outputs.dir(buildDir(target))
      commandLine("make", "install")
    }.also {
      println("REGISTERED BUILD TASK: $it")
    }
  }
}

/*
`configure' configures wolfssl 5.6.3 to adapt to many kinds of systems.

Usage: ./configure [OPTION]... [VAR=VALUE]...

To assign environment variables (e.g., CC, CFLAGS...), specify them as
VAR=VALUE.  See below for descriptions of some of the useful variables.

Defaults for the options are specified in brackets.

Configuration:
  -h, --help              display this help and exit
      --help=short        display options specific to this package
      --help=recursive    display the short help of all the included packages
  -V, --version           display version information and exit
  -q, --quiet, --silent   do not print `checking ...' messages
      --cache-file=FILE   cache test results in FILE [disabled]
  -C, --config-cache      alias for `--cache-file=config.cache'
  -n, --no-create         do not create output files
      --srcdir=DIR        find the sources in DIR [configure dir or `..']

Installation directories:
  --prefix=PREFIX         install architecture-independent files in PREFIX
                          [/usr/local]
  --exec-prefix=EPREFIX   install architecture-dependent files in EPREFIX
                          [PREFIX]

By default, `make install' will install all the files in
`/usr/local/bin', `/usr/local/lib' etc.  You can specify
an installation prefix other than `/usr/local' using `--prefix',
for instance `--prefix=$HOME'.

For better control, use the options below.

Fine tuning of the installation directories:
  --bindir=DIR            user executables [EPREFIX/bin]
  --sbindir=DIR           system admin executables [EPREFIX/sbin]
  --libexecdir=DIR        program executables [EPREFIX/libexec]
  --sysconfdir=DIR        read-only single-machine data [PREFIX/etc]
  --sharedstatedir=DIR    modifiable architecture-independent data [PREFIX/com]
  --localstatedir=DIR     modifiable single-machine data [PREFIX/var]
  --runstatedir=DIR       modifiable per-process data [LOCALSTATEDIR/run]
  --libdir=DIR            object code libraries [EPREFIX/lib]
  --includedir=DIR        C header files [PREFIX/include]
  --oldincludedir=DIR     C header files for non-gcc [/usr/include]
  --datarootdir=DIR       read-only arch.-independent data root [PREFIX/share]
  --datadir=DIR           read-only architecture-independent data [DATAROOTDIR]
  --infodir=DIR           info documentation [DATAROOTDIR/info]
  --localedir=DIR         locale-dependent data [DATAROOTDIR/locale]
  --mandir=DIR            man documentation [DATAROOTDIR/man]
  --docdir=DIR            documentation root [DATAROOTDIR/doc/wolfssl]
  --htmldir=DIR           html documentation [DOCDIR]
  --dvidir=DIR            dvi documentation [DOCDIR]
  --pdfdir=DIR            pdf documentation [DOCDIR]
  --psdir=DIR             ps documentation [DOCDIR]

Program names:
  --program-prefix=PREFIX            prepend PREFIX to installed program names
  --program-suffix=SUFFIX            append SUFFIX to installed program names
  --program-transform-name=PROGRAM   run sed PROGRAM on installed program names

System types:
  --build=BUILD     configure for building on BUILD [guessed]
  --host=HOST       cross-compile to build programs to run on HOST [BUILD]
  --target=TARGET   configure for building compilers for TARGET [HOST]

Optional Features:
  --disable-option-checking  ignore unrecognized --enable/--with options
  --disable-FEATURE       do not include FEATURE (same as --enable-FEATURE=no)
  --enable-FEATURE[=ARG]  include FEATURE [ARG=yes]
  --enable-dependency-tracking
                          do not reject slow dependency extractors
  --disable-dependency-tracking
                          speeds up one-time build
  --enable-silent-rules   less verbose build output (undo: "make V=1")
  --disable-silent-rules  verbose build output (undo: "make V=0")
  --enable-static[=PKGS]  build static libraries [default=no]
  --enable-shared[=PKGS]  build shared libraries [default=yes]
  --enable-fast-install[=PKGS]
                          optimize for fast installation [default=yes]
  --disable-libtool-lock  avoid locking (might break parallel builds)
  --enable-distro         Enable wolfSSL distro build (default: disabled)
  --enable-threadlocal    Enable thread local support (default: enabled)
  --enable-debug          Add debug code/turns off optimizations (yes|no)
                          [default=no]
  --enable-harden-tls     Enable requirements from RFC9325. Possible values
                          are <yes>, <112>, or <128>. <yes> is equivalent to
                          <112>. (default: disabled)
  --enable-32bit          Enables 32-bit support (default: disabled)
  --enable-16bit          Enables 16-bit support (default: disabled)
  --enable-64bit          Enables 64-bit support (default: disabled)
  --enable-kdf            Enables kdf support (default: enabled)
  --enable-hmac           Enables HMAC support (default: enabled)
  --enable-do178          Enable DO-178, Will NOT work w/o DO178 license
                          (default: disabled)
  --enable-asm            Enables option for assembly (default: enabled)
  --enable-fips           Enable FIPS 140-2, Will NOT work w/o FIPS license
                          (default: disabled)
  --enable-engine         Enable wolfEngine options (default: disabled)
  --enable-reproducible-build
                          Enable maximally reproducible build (default:
                          disabled)
  --enable-benchmark      Build benchmark when building crypttests (default:
                          enabled)
  --enable-linuxkm        Enable Linux Kernel Module (default: disabled)
  --enable-linuxkm-defaults
                          Enable feature defaults for Linux Kernel Module
                          (default: disabled)
  --enable-linuxkm-pie    Enable relocatable object build of Linux kernel
                          module (default: disabled)
  --enable-linuxkm-benchmarks
                          Enable crypto benchmarking autorun at module load
                          time for Linux kernel module (default: disabled)
  --enable-sp             Enable Single Precision maths implementation
                          (default: disabled)
  --enable-sp-math-all    Enable Single Precision math implementation for full
                          algorithm suite (default: enabled)
  --enable-sp-math        Enable Single Precision math implementation with
                          restricted algorithm suite (default: disabled)
  --enable-sp-asm         Enable Single Precision assembly implementation
                          (default: enabled on x86_64/aarch64/amd64)
  --enable-fastmath       Enable fast math ops (default: disabled)
  --enable-fasthugemath   Enable fast math + huge code (default: disabled)
  --enable-heapmath       Enable heap based integer.c math ops (default:
                          disabled)
  --enable-all            Enable all wolfSSL features, except SSLv3 (default:
                          disabled)
  --enable-all-crypto     Enable all wolfcrypt algorithms (default: disabled)
  --enable-kyber          Enable KYBER (default: disabled)
  --enable-singlethreaded Enable wolfSSL single threaded (default: disabled)
  --enable-rwlock         Enable use of rwlock (default: disabled)
  --enable-cryptonly      Enable wolfCrypt Only build (default: disabled)
  --enable-ech            Enable ECH (default: disabled)
  --enable-dtls           Enable wolfSSL DTLS (default: disabled)
  --enable-dtls-mtu       Enable setting the MTU size for wolfSSL DTLS
                          (default: disabled)
  --enable-tls13-draft18  Enable wolfSSL TLS v1.3 Draft 18 (default: disabled)
  --enable-tls13          Enable wolfSSL TLS v1.3 (default: enabled)
  --enable-quic           Enable QUIC API with wolfSSL TLS v1.3 (default:
                          disabled)
  --enable-postauth       Enable wolfSSL Post-handshake Authentication
                          (default: disabled)
  --enable-hrrcookie      Enable the server to send Cookie Extension in HRR
                          with state (default: disabled)
  --enable-rng            Enable compiling and using RNG (default: enabled)
  --enable-sctp           Enable wolfSSL DTLS-SCTP support (default: disabled)
  --enable-srtp           Enable wolfSSL DTLS-SRTP support (default: disabled)
  --enable-mcast          Enable wolfSSL DTLS multicast support (default:
                          disabled)
  --enable-bind           Enable Bind DNS compatibility build (default:
                          disabled)
  --enable-libssh2        Enable libssh2 compatibility build (default:
                          disabled)
  --enable-openssh        Enable OpenSSH compatibility build (default:
                          disabled)
  --enable-openvpn        Enable OpenVPN compatibility build (default:
                          disabled)
  --enable-openresty      Enable openresty (default: disabled)
  --enable-nginx          Enable nginx (default: disabled)
  --enable-chrony         Enable chrony support (default: disabled)
  --enable-openldap       Enable OpenLDAP support (default: disabled)
  --enable-lighty         Enable lighttpd/lighty (default: disabled)
  --enable-rsyslog        Enable rsyslog (default: disabled)
  --enable-haproxy        Enable haproxy (default: disabled)
  --enable-wpas           Enable wpa_supplicant support (default: disabled)
  --enable-wpas-dpp       Enable wpa_supplicant support with dpp (default:
                          disabled)
  --enable-ntp            Enable ntp support (default: disabled)
  --enable-fortress       Enable SSL fortress build (default: disabled)
  --enable-libwebsockets  Enable libwebsockets (default: disabled)
  --enable-net-snmp       Enable net-snmp (default: disabled)
  --enable-krb            Enable kerberos 5 support (default: disabled)
  --enable-ffmpeg         Enable FFmpeg support (default: disabled)
  --enable-ip-alt-name    Enable IP subject alternative name (default:
                          disabled)
  --enable-qt             Enable qt (default: disabled)
  --enable-bump           Enable SSL Bump build (default: disabled)
  --enable-sniffer        Enable wolfSSL sniffer support (default: disabled)
  --enable-signal         Enable signal (default: disabled)
  --enable-strongswan     Enable strongSwan support (default: disabled)
  --enable-opensslcoexist Enable coexistence of wolfssl/openssl (default:
                          disabled)
  --enable-smime          Enable S/MIME (default: disabled)
  --enable-psa            use Platform Security Architecture (PSA) interface
                          (default: disabled)
  --enable-psa-lib-static Link PSA as static library (default: disable)
  --enable-opensslall     Enable all OpenSSL API, size++ (default: disabled)
  --enable-opensslextra   Enable extra OpenSSL API, size+ (default: disabled)
  --enable-error-queue-per-thread
                          Enable one error queue per thread. Requires thread
                          local storage. (default: disabled)
  --enable-maxstrength    Enable Max Strength build, allows TLSv1.2-AEAD-PFS
                          ciphers only (default: disabled)
  --enable-harden         Enable Hardened build, Enables Timing Resistance and
                          Blinding (default: enabled)
  --enable-ipv6           Enable testing of IPV6 (default: disabled)
  --enable-leanpsk        Enable Lean PSK build (default: disabled)
  --enable-leantls        Enable Lean TLS build (default: disabled)
  --enable-lowresource    Enable low resource options for memory/flash
                          (default: disabled)
  --enable-titancache     Enable titan session cache (default: disabled)
  --enable-hugecache      Enable huge session cache (default: disabled)
  --enable-bigcache       Enable big session cache (default: disabled)
  --enable-smallcache     Enable small session cache (default: disabled)
  --enable-savesession    Enable persistent session cache (default: disabled)
  --enable-savecert       Enable persistent cert cache (default: disabled)
  --enable-writedup       Enable write duplication of WOLFSSL objects
                          (default: disabled)
  --enable-atomicuser     Enable Atomic User Record Layer (default: disabled)
  --enable-pkcallbacks    Enable Public Key Callbacks (default: disabled)
  --enable-aescbc         Enable wolfSSL AES-CBC support (default: enabled)
  --enable-aescbc-length-checks
                          Enable AES-CBC length validity checks (default:
                          disabled)
  --enable-aesgcm         Enable wolfSSL AES-GCM support (default: enabled)
  --enable-aesgcm-stream  Enable wolfSSL AES-GCM support with streaming APIs
                          (default: disabled)
  --enable-aesccm         Enable wolfSSL AES-CCM support (default: disabled)
  --enable-aessiv         Enable AES-SIV (RFC 5297) (default: disabled)
  --enable-aesctr         Enable wolfSSL AES-CTR support (default: disabled)
  --enable-aesofb         Enable wolfSSL AES-OFB support (default: disabled)
  --enable-aescfb         Enable wolfSSL AES-CFB support (default: disabled)
  --enable-armasm         Enable wolfSSL ARMv8 ASM support (default:
                          disabled). Set to sha512-crypto or sha3-crypto to
                          use SHA512 and SHA3 instructions with Aarch64 CPU.
  --enable-xilinx         Enable wolfSSL support for Xilinx hardened
                          crypto(default: disabled)
  --enable-caam           Enable wolfSSL support for CAAM (default: disabled)
  --enable-aesni          Enable wolfSSL AES-NI support (default: disabled)
  --enable-intelasm       Enable All Intel ASM speedups (default: disabled)
  --enable-aligndata      align data for ciphers (default: enabled)
  --enable-intelrand      Enable Intel rdrand as preferred RNG source
                          (default: disabled)
  --enable-amdrand        Enable AMD rdseed as preferred RNG seeding source
                          (default: disabled)
  --enable-afalg          Enable Linux af_alg use for crypto (default:
                          disabled)
  --enable-kcapi-hash     Enable libkcapi use for hashing (default: disabled)
  --enable-kcapi-hmac     Enable libkcapi use for HMAC (default: disabled)
  --enable-kcapi-aes      Enable libkcapi use for AES (default: disabled)
  --enable-kcapi-rsa      Enable libkcapi use for RSA (default: disabled)
  --enable-kcapi-dh       Enable libkcapi use for DH (default: disabled)
  --enable-kcapi-ecc      Enable libkcapi use for ECC (default: disabled)
  --enable-kcapi          Enable libkcapi use for crypto (default: disabled)
  --enable-devcrypto      Enable Linux dev crypto calls: all | aes (all aes
                          support) | hash (all hash algos) | cbc (aes-cbc
                          only) (default: disabled)
  --enable-camellia       Enable wolfSSL Camellia support (default: disabled)
  --enable-md2            Enable wolfSSL MD2 support (default: disabled)
  --enable-nullcipher     Enable wolfSSL NULL cipher support (default:
                          disabled)
  --enable-ripemd         Enable wolfSSL RIPEMD-160 support (default:
                          disabled)
  --enable-blake2         Enable wolfSSL BLAKE2b support (default: disabled)
  --enable-blake2s        Enable wolfSSL BLAKE2s support (default: disabled)
  --enable-sha224         Enable wolfSSL SHA-224 support (default: enabled on
                          x86_64/amd64/aarch64)
  --enable-sha3           Enable wolfSSL SHA-3 support (default: enabled on
                          x86_64/amd64/aarch64)
  --enable-shake128       Enable wolfSSL SHAKE128 support (default: disabled)
  --enable-shake256       Enable wolfSSL SHAKE256 support (default: disabled)
  --enable-sha512         Enable wolfSSL SHA-512 support (default: enabled)
  --enable-sha384         Enable wolfSSL SHA-384 support (default: enabled)
  --enable-sessioncerts   Enable session cert storing (default: disabled)
  --enable-keygen         Enable key generation (default: disabled)
  --enable-certgen        Enable cert generation (default: disabled)
  --enable-certreq        Enable cert request generation (default: disabled)
  --enable-certext        Enable cert request extensions (default: disabled)
  --enable-certgencache   Enable decoded cert caching (default: disabled)
  --enable-sep            Enable sep extensions (default: disabled)
  --enable-hkdf           Enable HKDF (HMAC-KDF) support (default: disabled)
  --enable-hpke           Enable HKPE support (default: disabled)
  --enable-x963kdf        Enable X9.63 KDF support (default: disabled)
  --enable-dsa            Enable DSA (default: disabled)
  --enable-eccshamir      Enable ECC Shamir (default: enabled)
  --enable-ecc            Enable ECC (default: enabled)
  --enable-ecccustcurves  Enable ECC custom curves (default: disabled)
  --enable-compkey        Enable compressed keys support (default: disabled)
  --enable-brainpool      Enable Brainpool ECC curves (default: enabled with
                          ECC custom curves)
  --enable-curve25519     Enable Curve25519 (default: disabled)
  --enable-ed25519        Enable ED25519 (default: disabled)
  --enable-ed25519-stream Enable wolfSSL ED25519 support with streaming verify
                          APIs (default: disabled)
  --enable-curve448       Enable Curve448 (default: disabled)
  --enable-ed448          Enable ED448 (default: disabled)
  --enable-ed448-stream   Enable wolfSSL ED448 support with streaming verify
                          APIs (default: disabled)
  --enable-fpecc          Enable Fixed Point cache ECC (default: disabled)
  --enable-eccencrypt     Enable ECC encrypt (default: disabled). yes = SEC1
                          standard, geniv = Generate IV, iso18033 = ISO 18033
                          standard, old = original wolfSSL algorithm
  --enable-eccsi          Enable ECCSI (default: disabled)
  --enable-sakke          Enable SAKKE - paring based crypto (default:
                          disabled)
  --enable-psk            Enable PSK (default: disabled)
  --enable-psk-one-id     Enable PSK (default: disabled)
  --enable-errorstrings   Enable error strings table (default: enabled)
  --enable-errorqueue     Disables adding nodes to error queue when compiled
                          with OPENSSL_EXTRA (default: enabled)
  --enable-oldtls         Enable old TLS versions < 1.2 (default: enabled)
  --enable-tlsv12         Enable TLS versions 1.2 (default: enabled)
  --enable-tlsv10         Enable old TLS versions 1.0 (default: disabled)
  --enable-sslv3          Enable SSL version 3.0 (default: disabled)
  --enable-stacksize      Enable stack size info on examples (default:
                          disabled)
  --enable-memory         Enable memory callbacks (default: enabled)
  --enable-trackmemory    Enable memory use info on wolfCrypt and wolfSSL
                          cleanup (default: disabled)
  --enable-memorylog      Enable dynamic memory logging (default: disabled)
  --enable-stacklog       Enable stack logging (default: disabled)
  --enable-wolfsentry     Enable wolfSentry hooks and plugins (default:
                          disabled)
  --enable-qt-test        Enable qt tests (default: disabled)
  --enable-rsa            Enable RSA (default: enabled)
  --enable-oaep           Enable RSA OAEP (default: enabled)
  --enable-rsapub         Enable RSA Public Only (default: disabled)
  --enable-rsavfy         Enable RSA Verify Inline Only (default: disabled)
  --enable-rsapss         Enable RSA-PSS (default: disabled)
  --enable-dh             Enable DH (default: enabled)
  --enable-anon           Enable Anonymous (default: disabled)
  --enable-asn            Enable ASN (default: enabled)
  --enable-asn-print      Enable ASN Print API (default: enabled)
  --enable-aes            Enable AES (default: enabled)
  --enable-dtls13         Enable wolfSSL DTLS v1.3 (default: disabled)
  --enable-dtlscid        Enable wolfSSL DTLS ConnectionID (default: disabled)
  --enable-coding         Enable Coding base 16/64 (default: enabled)
  --enable-base64encode   Enable Base64 encoding (default: enabled on
                          x86_64/amd64)
  --enable-base16         Enable Base16 encoding/decoding (default: disabled)
  --enable-des3           Enable DES3 (default: disabled)
  --enable-arc4           Enable ARC4 (default: disabled)
  --enable-md5            Enable MD5 (default: enabled)
  --enable-sha            Enable SHA (default: enabled)
  --enable-siphash        Enable SipHash (default: disabled)
  --enable-cmac           Enable CMAC (default: disabled)
  --enable-xts            Enable XTS (default: disabled)
  --enable-webserver      Enable Web Server (default: disabled)
  --enable-webclient      Enable Web Client (HTTP) (default: disabled)
  --enable-rc2            Enable RC2 encryption (default: disabled)
  --enable-selftest       Enable selftest, Will NOT work w/o CAVP selftest
                          license (default: disabled)
  --enable-poly1305       Enable wolfSSL POLY1305 support (default: enabled)
  --enable-chacha         Enable CHACHA (default: enabled). Use `=noasm` to
                          disable ASM AVX/AVX2 speedups
  --enable-xchacha        Enable XCHACHA (default: disabled).
  --enable-hashdrbg       Enable Hash DRBG support (default: enabled)
  --enable-entropy-memuse Enable memuse entropy support (default: disabled)
  --enable-filesystem     Enable Filesystem support (default: enabled)
  --enable-inline         Enable inline functions (default: enabled)
  --enable-ocsp           Enable OCSP (default: disabled)
  --enable-ocspstapling   Enable OCSP Stapling (default: disabled)
  --enable-ocspstapling2  Enable OCSP Stapling v2 (default: disabled)
  --enable-crl            Enable CRL (Use =io for inline CRL HTTP GET)
                          (default: disabled)
  --enable-crl-monitor    Enable CRL Monitor (default: disabled)
  --enable-sni            Enable SNI (default: disabled)
  --enable-maxfragment    Enable Maximum Fragment Length (default: disabled)
  --enable-alpn           Enable ALPN (default: disabled)
  --enable-trustedca      Enable Trusted CA Indication (default: disabled)
  --enable-truncatedhmac  Enable Truncated HMAC (default: disabled)
  --enable-renegotiation-indication
                          Enable Renegotiation Indication for client via empty
                          cipher (default: disabled)
  --enable-secure-renegotiation
                          Enable Secure Renegotiation (default: disabled)
  --enable-secure-renegotiation-info
                          Enable Secure Renegotiation info extension (default:
                          enabled)
  --enable-fallback-scsv  Enable Fallback SCSV (default: disabled)
  --enable-keying-material
                          Enable Keying Material Exporters (default: disabled)
  --enable-supportedcurves
                          Enable Supported Elliptic Curves (default: enabled)
  --enable-ffdhe-only     Enable using only FFDHE in client (default:
                          disabled)
  --enable-session-ticket Enable Session Ticket (default: disabled)
  --enable-ticket-nonce-malloc
                          Enable dynamic allocation of ticket nonces (default:
                          disabled)
  --enable-extended-master
                          Enable Extended Master Secret (default: enabled)
  --enable-tlsx           Enable all TLS Extensions (default: disabled)
  --enable-earlydata      Enable Early Data handshake with wolfSSL TLS v1.3
                          (default: disabled)
  --enable-pkcs7          Enable PKCS7 (default: disabled)
  --enable-wolfssh        Enable wolfSSH options (default: disabled)
  --enable-ssh            Enable wolfSSH options (default: disabled)
  --enable-wolftpm        Enable wolfTPM options (default: disabled)
  --enable-wolfclu        Enable wolfCLU options (default: disabled)
  --enable-scep           Enable wolfSCEP (default: disabled)
  --enable-srp            Enable Secure Remote Password (default: disabled)
  --enable-indef          Enable parsing of indefinite length encoded msgs
                          (default: disabled)
  --enable-altcertchains  Enable using alternative certificate chains, only
                          require leaf certificate to validate to trust root
                          (default: disabled)
  --enable-smallstackcache
                          Enable Small Stack Usage Caching (default: disabled)
  --enable-smallstack     Enable Small Stack Usage (default: disabled)
  --enable-valgrind       Enable valgrind for unit tests (default: disabled)
  --enable-testcert       Enable Test Cert (default: disabled)
  --enable-iopool         Enable I/O Pool example (default: disabled)
  --enable-certservice    Enable cert service (default: disabled)
  --enable-jni            Enable wolfSSL JNI (default: disabled)
  --enable-asio           Enable asio (default: disabled)
  --enable-apachehttpd    Enable Apache httpd (default: disabled)
  --enable-enc-then-mac   Enable Encrypt-Then-Mac extension (default: enabled)
  --enable-stunnel        Enable stunnel (default: disabled)
  --enable-curl           Enable curl (default: disabled)
  --enable-tcpdump        Enable tcpdump (default: disabled)
  --enable-sblim-sfcb     Enable sblim-sfcb support (default: disabled)
  --enable-libest         Enable libest (default: disabled)
  --enable-md4            Enable MD4 (default: disabled)
  --enable-enckeys        Enable PEM encrypted private key support (default:
                          disabled)
  --enable-pkcs12         Enable pkcs12 (default: enabled)
  --enable-pwdbased       Enable PWDBASED (default: disabled)
  --enable-scrypt         Enable SCRYPT (default: disabled)
  --enable-examples       Enable Examples (default: enabled)
  --enable-crypttests     Enable Crypt Bench/Test (default: enabled)
  --enable-crypttests-libs
                          Enable wolfcrypt test and benchmark libraries
                          (default: disabled)
  --enable-pkcs11         Enable pkcs11 access (default: disabled)
  --enable-pkcs8          Enable PKCS #8 key packages (default: enabled)
  --enable-fast-rsa       Enable RSA using Intel IPP (default: disabled)
  --enable-staticmemory   Enable static memory use (default: disabled)
  --enable-mcapi          Enable Microchip API (default: disabled)
  --enable-asynccrypt     Enable Asynchronous Crypto (default: disabled)
  --enable-asynccrypt-sw  Enable asynchronous software-based crypto (default:
                          disabled)
  --enable-asyncthreads   Enable Asynchronous Threading (default: enabled)
  --enable-cryptodev      DEPRECATED, use cryptocb instead
  --enable-cryptocb       Enable crypto callbacks (default: disabled)
  --enable-sessionexport  Enable export and import of sessions (default:
                          disabled)
  --enable-aeskeywrap     Enable AES key wrap support (default: disabled)
  --enable-oldnames       Keep backwards compat with old names (default:
                          enabled)
  --enable-memtest        Memory testing option, for internal use (default:
                          disabled)
  --enable-hashflags      Enable support for hash flags (default: disabled)
  --enable-dhdefaultparams
                          Enables option for default dh parameters (default:
                          disabled)
  --enable-context-extra-user-data
                          Enables option for storing user-defined data in TLS
                          API contexts, with optional argument the number of
                          slots to allocate (default: disabled)
  --enable-iotsafe        Enables support for IoT-Safe secure applet (default:
                          disabled)
  --enable-iotsafe-hwrng  Enables support for IoT-Safe RNG (default: disabled)
  --enable-makeclean      Enables forced "make clean" at the end of configure
                          (default: enabled)
  --enable-usersettings   Use your own user_settings.h and do not add Makefile
                          CFLAGS (default: disabled)
  --enable-optflags       Enable default optimization CFLAGS for the compiler
                          (default: enabled)
  --enable-sys-ca-certs   Enable ability to load CA certs from OS (default:
                          enabled)
  --enable-jobserver[=no/yes/#] default=yes
                        Enable up to # make jobs
                        yes: enable one more than CPU count


Optional Packages:
  --with-PACKAGE[=ARG]    use PACKAGE [ARG=yes]
  --without-PACKAGE       do not use PACKAGE (same as --with-PACKAGE=no)
  --with-pic[=PKGS]       try to use only PIC/non-PIC objects [default=use
                          both]
  --with-aix-soname=aix|svr4|both
                          shared library versioning (aka "SONAME") variant to
                          provide on AIX, [default=aix].
  --with-gnu-ld           assume the C compiler uses GNU ld [default=no]
  --with-sysroot[=DIR]    Search for dependent libraries within DIR (or the
                          compiler's sysroot if not specified).
  --with-linux-source=PATH
                          PATH to root of Linux kernel build tree
  --with-linux-arch=arch  built arch (SRCARCH) of Linux kernel build tree
  --with-liboqs=PATH      Path to liboqs install (default /usr/local)
                          EXPERIMENTAL!
  --with-psa-include=PATH PATH to directory with PSA header files
  --with-psa-lib=PATH     PATH to directory with the PSA library
  --with-psa-lib-name=NAME
                          NAME of PSA library
  --with-maxq10xx=PART    MAXQ10XX PART Number
  --with-cryptoauthlib=PATH
                          PATH to CryptoAuthLib install (default /usr/)
  --with-se050=PATH       PATH to SE050 install (default /usr/local)
  --with-seco=PATH        PATH to SECO install (default /usr/lib/)
  --with-eccminsz=BITS    Sets the ECC minimum key size (default: 224 bits)
  --with-wolfsentry=PATH  PATH to directory with wolfSentry installation
  --with-wolfsentry-lib=PATH
                          PATH to directory with wolfSentry library
  --with-wolfsentry-include=PATH
                          PATH to directory with wolfSentry header files
  --with-user-crypto=PATH Path to USER_CRYPTO install (default /usr/local)
  --with-wnr=PATH         Path to Whitewood netRandom install (default
                          /usr/local)
  --with-libz=PATH        PATH to libz install (default /usr/)
  --with-cavium=PATH      PATH to cavium/software dir
  --with-cavium-v=PATH    PATH to Cavium V/software dir
  --with-octeon-sync=PATH PATH to Cavium Octeon SDK dir (sync)
  --with-intelqa=PATH     PATH to Intel QuickAssist (QAT) driver dir
  --with-intelqa-sync=PATH
                          PATH to Intel QuickAssist (QAT) driver dir (sync)
  --with-arm-target=x     x can be "thumb" or "cortex"
  --with-max-rsa-bits=number
                          number of bits to support for RSA, DH, and DSA keys
  --with-max-ecc-bits=number
                          number of bits to support for ECC algorithms
  --with-libsuffix=SUFFIX Library artifact SUFFIX, ie libwolfsslSUFFIX.so

Some influential environment variables:
  CC          C compiler command
  CFLAGS      C compiler flags
  LDFLAGS     linker flags, e.g. -L<lib dir> if you have libraries in a
              nonstandard directory <lib dir>
  LIBS        libraries to pass to the linker, e.g. -l<library>
  CPPFLAGS    (Objective) C/C++ preprocessor flags, e.g. -I<include dir> if
              you have headers in a nonstandard directory <include dir>
  LT_SYS_LIBRARY_PATH
              User-defined run-time library search path.
  EXTRA_CPPFLAGS
              Extra CPPFLAGS to add to end of autoconf-computed arg list. Can
              also supply directly to make.
  EXTRA_CFLAGS
              Extra CFLAGS to add to end of autoconf-computed arg list. Can
              also supply directly to make.
  EXTRA_CCASFLAGS
              Extra CCASFLAGS to add to end of autoconf-computed arg list. Can
              also supply directly to make.
  EXTRA_LDFLAGS
              Extra LDFLAGS to add to end of autoconf-computed arg list. Can
              also supply directly to make.
  CCAS        assembler compiler command (defaults to CC)
  CCASFLAGS   assembler compiler flags (defaults to CFLAGS)
  CPP         C preprocessor

Use these variables to override the choices made by `configure' or to help
it to find libraries and programs with nonstandard names/locations.

Report bugs to <https://github.com/wolfssl/wolfssl/issues>.
wolfssl home page: <https://www.wolfssl.com>.
*/
