# Start from the latest golang base image
FROM golang:latest

# Create and set the Current Working Directory inside the container
RUN mkdir /app
WORKDIR /app

# Copy go mod and sum files
COPY go.mod go.sum ./

# Download all dependencies. Dependencies will be cached if the go.mod and go.sum files are not changed
RUN go mod download 
COPY . .

# Build to a binary named build
RUN go build -o build .

# Expose port 8080 to the outside world
EXPOSE 8080

# Command to run the executable
CMD ["./build"]
