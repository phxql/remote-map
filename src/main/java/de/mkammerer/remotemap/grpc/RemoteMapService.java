package de.mkammerer.remotemap.grpc;

import com.google.protobuf.ByteString;
import de.mkammerer.remotemap.grpc.generated.DeleteRequest;
import de.mkammerer.remotemap.grpc.generated.DeleteResponse;
import de.mkammerer.remotemap.grpc.generated.GetRequest;
import de.mkammerer.remotemap.grpc.generated.GetResponse;
import de.mkammerer.remotemap.grpc.generated.PutRequest;
import de.mkammerer.remotemap.grpc.generated.PutResponse;
import de.mkammerer.remotemap.grpc.generated.RemoteMapGrpc;
import de.mkammerer.remotemap.storage.Storage;
import io.grpc.stub.StreamObserver;

public class RemoteMapService extends RemoteMapGrpc.RemoteMapImplBase {
    private final Storage storage;

    public RemoteMapService(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        storage.put(
            request.getKey().toByteArray(),
            request.getValue().toByteArray()
        );

        responseObserver.onNext(PutResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void get(GetRequest request, StreamObserver<GetResponse> responseObserver) {
        byte[] bytes = storage.get(
            request.getKey().toByteArray()
        );

        if (bytes == null) {
            responseObserver.onNext(GetResponse.getDefaultInstance());
        } else {
            responseObserver.onNext(GetResponse.newBuilder().setValue(ByteString.copyFrom(bytes)).build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
        storage.delete(
            request.getKey().toByteArray()
        );

        responseObserver.onNext(DeleteResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
