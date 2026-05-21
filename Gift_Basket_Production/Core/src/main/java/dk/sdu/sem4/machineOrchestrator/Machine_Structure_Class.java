package dk.sdu.sem4.machineOrchestrator;

public class Machine_Structure_Class
{

    // Machine identity - identifies the physical machine (1 to 255)
    private int machine_Structure_ID;
    private String machine_Structure_Type;
    private Machine_Structure_Status_Enum machine_Structure_Status;
    private Machine_Process_States_Enum machine_Structure_State;

    // Component identity - identifies the component connected to this machine
    private int component_ID;
    private String component_Type;
    private Component_Status_Enum component_Status;
    private Component_Process_States_Enum component_State;

    // Component Simulation - configurer the structure to simulate the component.
    private boolean simulate_Component;
    private boolean simulate_Component_SuccessfulOutput;






    // Constructor - called via super() from subclasses
    public Machine_Structure_Class(int machine_Structure_ID, String machine_Structure_Type)
    {

        // .
        if (this.Check_MachineStructure_ID_isValid(machine_Structure_ID))
        {
            this.machine_Structure_ID = machine_Structure_ID;
        }
        else
        {
            // Could add a System message / Error message.
            this.machine_Structure_ID = 0;
        }

        if (this.Check_MachineStructure_Type_isValid(machine_Structure_Type))
        {
            this.machine_Structure_Type = machine_Structure_Type;
        }
        else
        {
            // Could add a System message / Error message.
            this.machine_Structure_Type = "Generic Machine Type";
        }

        this.machine_Structure_Status = Machine_Structure_Status_Enum.NONE;
        this.machine_Structure_State = Machine_Process_States_Enum.CONSTRUCTED;


        // .
        this.component_ID = -2;
        this.component_Type = "";
        this.component_Status = null;
        this.component_State = null;


        // .
        this.simulate_Component = false;
        this.simulate_Component_SuccessfulOutput = true;

    }


    public Machine_Structure_Class(int machine_Structure_ID, String machine_Structure_Type, boolean simulate_Component , boolean simulate_Component_SuccessfulOutput)
    {

        // .
        if (this.Check_MachineStructure_ID_isValid(machine_Structure_ID))
        {
            this.machine_Structure_ID = machine_Structure_ID;
        }
        else
        {
            // Could add a System message / Error message.
            this.machine_Structure_ID = 0;
        }

        if (this.Check_MachineStructure_Type_isValid(machine_Structure_Type))
        {
            this.machine_Structure_Type = machine_Structure_Type;
        }
        else
        {
            // Could add a System message / Error message.
            this.machine_Structure_Type = "Generic Machine Type";
        }

        this.machine_Structure_Status = Machine_Structure_Status_Enum.NONE;
        this.machine_Structure_State = Machine_Process_States_Enum.CONSTRUCTED;


        // .
        this.component_ID = -2;
        this.component_Type = "";
        this.component_Status = null;
        this.component_State = null;

        // .
        this.simulate_Component = simulate_Component;
        this.simulate_Component_SuccessfulOutput = simulate_Component_SuccessfulOutput;

        // Set Component ID to zero, to mark the Component as being simulated.
        if (this.simulate_Component)
        {
            this.component_ID = 0;
        }

    }












    // --- Protected helpers - internal access for subclasses only ---


    // Machine

    protected int Get_Machine_Structure_ID()
    {
        return this.machine_Structure_ID;
    }

    protected String Get_Machine_Structure_Type()
    {
        return this.machine_Structure_Type;
    }

    protected Machine_Structure_Status_Enum Get_Machine_Structure_Status()
    {
        return this.machine_Structure_Status;
    }

    protected Machine_Process_States_Enum Get_Machine_Structure_State()
    {
        return this.machine_Structure_State;
    }

    protected boolean Set_Machine_Structure_Status(Machine_Structure_Status_Enum new_machine_Status)
    {
        if (this.Check_MachineStructure_Status_isValid(new_machine_Status))
        {
            this.machine_Structure_Status = new_machine_Status;

            // Checks to see if it really has been updated.
            if ((this.machine_Structure_Status).compareTo(new_machine_Status) == 0)
            {
                return true;
            }
        }
        return false;
    }

    protected boolean Set_Machine_Structure_State(Machine_Process_States_Enum new_machine_State)
    {
        if (this.Check_MachineStructure_State_isValid(new_machine_State))
        {
            this.machine_Structure_State = new_machine_State;

            // Checks to see if it really has been updated.
            if ((this.machine_Structure_State).compareTo(new_machine_State) == 0)
            {
                return true;
            }
        }
        return false;
    }







    // Component

    protected int Get_Component_ID()
    {
        return this.component_ID;
    }

    protected String Get_Component_Type()
    {
        return this.component_Type;
    }

    protected Component_Status_Enum Get_Component_Status()
    {
        return this.component_Status;
    }

    protected Component_Process_States_Enum Get_Component_State()
    {
        return this.component_State;
    }

    protected boolean Set_Component_ID(int new_component_ID)
    {
        if (this.Check_ComponentID_isValid(new_component_ID))
        {
            this.component_ID = new_component_ID;

            // Checks to see if it really has been updated.
            if (this.component_ID == new_component_ID)
            {
                return true;
            }
        }
        return false;
    }

    protected boolean Set_Component_Type(String new_component_Type)
    {
        if (this.Check_ComponentType_isValid(new_component_Type))
        {
            this.component_Type = new_component_Type;

            // Checks to see if it really has been updated.
            if ((this.component_Type).compareTo(new_component_Type) == 0)
            {
                return true;
            }
        }
        return false;
    }

    protected boolean Set_Component_Status(Component_Status_Enum new_component_Status)
    {
        if (this.Check_ComponentStatus_isValid(new_component_Status))
        {
            this.component_Status = new_component_Status;

            // Checks to see if it really has been updated.
            if ((this.component_Status).compareTo(new_component_Status) == 0)
            {
                return true;
            }
        }
        return false;
    }

    protected boolean Set_Component_State(Component_Process_States_Enum new_component_State)
    {
        if (this.Check_ComponentState_isValid(new_component_State))
        {
            this.component_State = new_component_State;

            // Checks to see if it really has been updated.
            if ((this.component_State).compareTo(new_component_State) == 0)
            {
                return true;
            }
        }
        return false;
    }







    // Machine Checks.

    private boolean Check_MachineStructure_ID_isValid(int machine_id)
    {
        if (machine_id < 0)
        {
            // Could add a System message / Error message.
            return false;
        }
        if (machine_id == 0)
        {
            // "Machine ID = 0" is always skipped.
            // Related to: "Component ID = 0" is reserved for Simulation.
            // Could add a System message.
            return false;
        }
        if ( machine_id > 0 )
        {
            return true;
        }
        return false;
    }

    private boolean Check_MachineStructure_Type_isValid(String machine_type)
    {
        if (machine_type == null)
        {
            // Could add a System message / Error message.
            return false;
        }
        if (machine_type.isEmpty())
        {
            // Could add a System message / Error message.
            return false;
        }
        if (true) // Need to add more logic, if making use of enums.
        {
            return true;
        }
        return false;
    }

    private boolean Check_MachineStructure_Status_isValid(Machine_Structure_Status_Enum machine_status)
    {
        if (machine_status == null)
        {
            // Could add a System message / Error message.
            return false;
        }
        if ((machine_status.toString()).isEmpty())
        {
            // Could add a System message / Error message.
            return false;
        }
        if (true) // Might add some more logic.
        {
            return true;
        }
        return false;
    }

    private boolean Check_MachineStructure_State_isValid(Machine_Process_States_Enum machine_state)
    {
        if (machine_state == null)
        {
            // Could add a System message / Error message.
            return false;
        }
        if ((machine_state.toString()).isEmpty())
        {
            // Could add a System message / Error message.
            return false;
        }
        if (true) // Might add some more logic.
        {
            return true;
        }
        return false;
    }






    // Component Checks.

    protected boolean Check_Component_isValid(int component_id, String component_type)
    {
        if (this.Check_ComponentID_isValid(component_id) && this.Check_ComponentType_isValid(component_type))
        {
            return true;
        }
        return false;
    }

    protected boolean Check_ComponentID_isValid(int component_id)
    {
        if (component_id < 0)
        {
            // Could add a System message / Error message.
            return false;
        }
        if (component_id == 0)
        {
            // "Component ID = 0" is reserved for Simulation.
            // Could add a System message.
            return false;
        }
        if (component_id > 0)
        {
            return true;
        }
        return false;
    }

    protected boolean Check_ComponentType_isValid(String component_type)
    {
        if (component_type == null)
        {
            // Could add a System message / Error message.
            return false;
        }
        if (component_type.isEmpty())
        {
            // Could add a System message / Error message.
            return false;
        }
        if (true) // Need to add more logic, if making use of enums.
        {
            return true;
        }
        return false;
    }

    protected boolean Check_ComponentStatus_isValid(Component_Status_Enum component_status)
    {
        if (component_status == null)
        {
            // Could add a System message / Error message.
            return false;
        }
        if ((component_status.toString()).isEmpty())
        {
            // Could add a System message / Error message.
            return false;
        }
        if (true) // Might add some more logic.
        {
            return true;
        }
        return false;
    }

    protected boolean Check_ComponentState_isValid(Component_Process_States_Enum component_state)
    {
        if (component_state == null)
        {
            // Could add a System message / Error message.
            return false;
        }
        if ((component_state.toString()).isEmpty())
        {
            // Could add a System message / Error message.
            return false;
        }
        if (true) // Might add some more logic.
        {
            return true;
        }
        return false;
    }






    // Component Simulation

    protected boolean Get_simulate_Component_State()
    {
        return this.simulate_Component;
    }

    protected boolean Get_simulate_Component_SuccessfulOutput_State()
    {
        return this.simulate_Component_SuccessfulOutput;
    }



}