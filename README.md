# Remote Map

## What is this?

A `ConcurrentHashMap` with a gRPC service on top of it - a HashMap with a network interface, so to say.
The map is persisted on disk, too.

## Why?

For fun and curiosity. It doesn't serve any deeper purpose.

## Where's the gRPC service definition?

[Here you go](remote-map-server/src/main/proto/RemoteMap.proto)

## Developing

### Building

```
./mvnw clean package
```

Run the `de.mkammerer.remotemap.Server.main` method in your IDE to start the server.

Run the `de.mkammerer.remotemap.client.Client.main` method in your IDE to start the client.

## License

[GPLv3](https://www.gnu.org/licenses/gpl-3.0.html)