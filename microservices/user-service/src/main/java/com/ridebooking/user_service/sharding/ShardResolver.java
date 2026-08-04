package com.ridebooking.user_service.sharding;



public class ShardResolver {

    private ShardResolver() {
    }

    public static String getShard(String email) {

        int shard = Math.abs(email.toLowerCase().hashCode()) % 4;

        switch (shard) {

            case 0:
                return "SHARD1";

            case 1:
                return "SHARD2";

            case 2:
                return "SHARD3";

            default:
                return "SHARD4";
        }
    }
}
