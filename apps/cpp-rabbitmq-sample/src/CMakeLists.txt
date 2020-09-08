cmake_minimum_required(VERSION 3.2 FATAL_ERROR)

if(POLICY CMP0025)
    cmake_policy(SET CMP0025 NEW)
endif()

add_library(poco_simple_handler SimplePocoHandler.cpp SimplePocoHandler.h)
target_link_libraries(poco_simple_handler PocoNet PocoFoundation)

set(PROGS receive
          send
)

find_package(mongocxx REQUIRED)
include_directories(${LIBMONGOCXX_INCLUDE_DIR})
include_directories(${LIBBSONCXX_INCLUDE_DIR})
include_directories("/usr/local/include/mongocxx/v_noabi")
include_directories("/usr/local/include/bsoncxx/v_noabi")
include_directories("/usr/local/include/libmongoc-1.0")
include_directories("/usr/local/include/libbson-1.0")
include_directories("/usr/local/lib")

foreach(item ${PROGS})
    add_executable(${item} "${item}.cpp" "utils.cpp")
    target_link_libraries(${item} amqp-cpp poco_simple_handler mongo::mongocxx_static)
endforeach(item)