#include <iostream>
#include "utils.hpp"
using namespace std;

string getIPaddress() {
    struct ifaddrs * ifAddrStruct=NULL;
    struct ifaddrs * ifa=NULL;
    void * tmpAddrPtr=NULL;

    getifaddrs(&ifAddrStruct);
    for (ifa = ifAddrStruct; ifa != NULL; ifa = ifa->ifa_next) {
        if (!ifa->ifa_addr) {
            continue;
        }
        if (ifa->ifa_addr->sa_family == AF_INET) { // check it is IP4
            // is a valid IP4 Address
            tmpAddrPtr=&((struct sockaddr_in *)ifa->ifa_addr)->sin_addr;
            char addressBuffer[INET_ADDRSTRLEN];
            inet_ntop(AF_INET, tmpAddrPtr, addressBuffer, INET_ADDRSTRLEN);
            if (strcmp(ifa->ifa_name, "eth0") == 0 || strcmp(ifa->ifa_name, "en0") == 0) {
                return addressBuffer;
            }
        }
    }
    if (ifAddrStruct!=NULL) freeifaddrs(ifAddrStruct);
    return "null";
}

string getCurrentTime() {
    const int NUM_YEAR = 4;
    const int DELETE_YEAR_and_TIME = 13;
    const boost::posix_time::ptime posix_local_time = boost::posix_time::microsec_clock::local_time();
    const boost::posix_time::time_duration td = posix_local_time.time_of_day();

    const long hours        = td.hours();
    const long minutes      = td.minutes();
    const long seconds      = td.seconds();
    const long milliseconds = td.total_milliseconds() - ((hours * 3600 + minutes * 60 + seconds) * 1000);

    //HH:MM:SS.SSS
    char buf[40];
    sprintf(buf, "%02ld:%02ld:%02ld.%03ld", hours, minutes, seconds, milliseconds);

    auto now = chrono::system_clock::now();
    time_t now_time = chrono::system_clock::to_time_t(now);
    //e.g. Fri Aug 14 16:12:08 2020
    string date = ctime(&now_time);
    date.pop_back();
    string year = date.substr(date.length() - NUM_YEAR);
    //e.g. Fri Aug 14 
    date.erase(date.length() - DELETE_YEAR_and_TIME);
    return date + year + " " + buf;
}