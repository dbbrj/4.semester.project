package dk.sdu.sem4.AGV;

public class AGVController
{


    private AGVAdapter adapter;






    public AGVController(AGVAdapter adapter) {
        this.adapter = adapter;
    }




    public void sendCommand(String genericCommand) {

        // Convert generic → specific
        String specificCommand = convertCommand(genericCommand);

        // Send to adapter
        adapter.sendToAGV(specificCommand);
    }

    private String convertCommand(String command) {

        if (command.equals("MOVE_TO_ASSEMBLY")) {
            return "MoveToAssemblyOperation";
        }

        if (command.equals("MOVE_TO_CHARGER")) {
            return "MoveToChargerOperation";
        }

        return "UNKNOWN";
    }



}
