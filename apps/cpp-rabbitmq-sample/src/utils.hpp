#ifndef _UTILS_
#define _UTILS_

#include <iostream>
#include <string>
#include <cstdio>
#include <cstdlib>
#include <chrono>
#include <ctime>   
#include <sys/types.h>
#include <ifaddrs.h>
#include <netinet/in.h> 
#include <arpa/inet.h>
#include "boost/date_time/posix_time/posix_time.hpp"

using namespace std;
string getIPaddress();
string getCurrentTime();

#endif