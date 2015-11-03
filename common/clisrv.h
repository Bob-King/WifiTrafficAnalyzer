/**
 *
 */


#define UDP_SERVER_PORT (0x431)
#define MAX_UDP_BUFFER (1024)

#ifdef __GNUC__
#define UNUSED(x) UNUSED_ ## x __attribute__((__unused__))
#else
#define UNUSED(x) UNUSED_ ## x
#endif
