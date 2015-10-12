/**
 *
 */


#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <unistd.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>


#include "clisrv.h"

int main(int argc, char *argv[])
{
	struct sockaddr_in srv;
	int sockfd;
	char buffer[MAX_UDP_BUFFER];
	int i;
	int n;

	if (argc < 2)
	{
		fprintf(stdout, "Usage: %s <IP address of server>\n", argv[0]);
		return -1;
	}

	sockfd = socket(PF_INET, SOCK_DGRAM, 0);
	if (sockfd < 0)
	{
		fprintf(stderr, "Failed to create socket file!\n");
		return -2;
	}

	memset(&srv, 0, sizeof(srv));
	srv.sin_family = AF_INET;
	srv.sin_addr.s_addr = inet_addr(argv[1]);
	srv.sin_port = UDP_SERVER_PORT;

	for (i = 0; i != 1024; ++i)
	{
		sprintf(buffer, "Packet %x\n", i);

		fprintf(stdout, "Sending %s ...\n", buffer);

		if (sendto(sockfd, buffer, strlen(buffer), 0, (struct sockaddr *) &srv, sizeof(srv)) != strlen(buffer))
		{
			fprintf(stderr, "Failed to send packet %x!\n", i);
		}
	}

	return 0;
}

