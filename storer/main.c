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
	char buffer[MAX_UDP_BUFFER + 1];
	int n;
	char *path;
	FILE *file;
	int c;

	path = NULL;
	while ((c = getopt(argc, argv, "o:")) != -1)
	{
		switch (c)
		{
			case 'o':
				path = optarg;
				break;
		}
	}

	if (path)
	{
		file = fopen(path, "a");
		if (!file)
		{
			fprintf(stderr, "Failed to open database file(%s)!\n", path);
			return -1;
		}
	}
	else
	{
		file = stdout;
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
	srv.sin_port = htons(UDP_SERVER_PORT);

	if (bind(sockfd, (struct sockaddr *) &srv, sizeof(srv)) < 0)
	{
		fprintf(stderr, "Failed to bind udp server!\n");
		return -2;
	}

	printf("Listening port %d\n", (int)srv.sin_port);

	for ( ; ; )
	{
		n = recvfrom(sockfd, buffer, sizeof(buffer), 0, NULL, NULL);
		if (n < 0)
		{
			fprintf(stderr, "Some error occurs when receiving data!\n");
			continue;
		}

		if (n >= (int)sizeof(buffer))
		{
			fprintf(stderr, "The recived data is out of length. Drop it!\n");
			continue;
		}

		buffer[n] = '\0';

		fprintf(file, "%s\n", buffer);
	}

	return 0;
}

