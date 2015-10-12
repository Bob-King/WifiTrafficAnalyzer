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

#define DATABASE_FILE "/var/run/wta/db.txt"

int main(int argc, char *argv[])
{
	struct sockaddr_in srv;
	int sockfd;
	char buffer[MAX_UDP_BUFFER + 1];
	int n;
	FILE *file;

	file = fopen(DATABASE_FILE, "a");
	if (!file)
	{
		fprintf(stderr, "Failed to open database file!\n");
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
	srv.sin_addr.s_addr = htonl(INADDR_ANY);
	srv.sin_port = UDP_SERVER_PORT;

	if (bind(sockfd, (struct sockaddr *) &srv, sizeof(srv)) < 0)
	{
		fprintf(stderr, "Failed to bind udp server!\n");
		return -2;
	}

	for ( ; ; )
	{
		n = recvfrom(sockfd, buffer, sizeof(buffer), 0, NULL, NULL);
		if (n < 0)
		{
			fprintf(stderr, "Some error occurs when receiving data!\n");
			continue;
		}

		if (n >= sizeof(buffer))
		{
			fprintf(stderr, "The recived data is out of length. Drop it!\n");
			continue;
		}

		buffer[n] = '\0';

		fprintf(stdout, "Received: %s\n", buffer);

		fputs(buffer, file);
	}

	return 0;
}

