package integration.test;

import integration.test.types.*;

public class Test {

    public static void main() {
        new UnnamedQuery().getVariables();
        new TrackingInfoQuery().getVariables().setItemId("ID");
        new ItemsInDeliveryByPaymentQuery().getVariables().setPaidBy(PaymentType.CASH);
        SoldItem item = new SoldItem();
        item.setItem("Item");
        item.setPaidBy(PaymentType.OTHER);
        item.setTrack("Track");
        new DeliveredMutation().getVariables().setItem(item);
    }
}