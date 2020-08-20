package integration.test;

import integration.test.types.*;

public class Test {

    public static void main() {
        new UnnamedQuery()
                .getVariables();
        new TrackingInfoQuery()
                .getVariables()
                .setItemId("ID");
        new ItemsInDeliveryByPaymentQuery()
                .getVariables()
                .setPaidBy(PaymentType.CASH);
        new DeliveredMutation()
                .getVariables()
                .setItem(new SoldItem()
                        .setItem("Item")
                        .setPaidBy(PaymentType.OTHER)
                        .setTrack("Track"));
    }
}