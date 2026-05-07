package dk.sdu.sem4.AGV;

public class AGVAdapter {

    private String agvUrl;

    public AGVAdapter(String agvUrl) {
        this.agvUrl = agvUrl;
    }

    public void sendToAGV(String specificCommand) {

        // Simulate sending REST request
        System.out.println("Sending to AGV at " + agvUrl);
        System.out.println("Command: " + specificCommand);

        // In real system → HTTP PUT request here
    }

    /*AGVControllerService agvControllerService;

    public AGVAdapter(AGVController controller) {
        this.agvControllerService = new AGVControllerService(controller);
    }

    public AGVControllerService getAGVControllerService() {
        return agvControllerService;
    }*/

}
