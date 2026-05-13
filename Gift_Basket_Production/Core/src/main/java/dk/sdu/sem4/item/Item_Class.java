package dk.sdu.sem4.item;

import java.util.ArrayList;

public class Item_Class
{

    /**
     *
     */
    private int id;

    /**
     *
     */
    private String name;

    /**
     *
     */
    private ArrayList<String> inventory_ID;

    /**
     *
     */
    private boolean valid_Inventory_ID_Format;




    /**
     *
     * @param id
     * @param name
     * @param inventory_ID
     */
    public Item_Class (int id, String name, String inventory_ID)
    {
        this.id = id;
        this.name = name;
        this.inventory_ID = new ArrayList<String>();
        this.inventory_ID.add(inventory_ID);
        this.valid_Inventory_ID_Format = Validate_Inventory_ID_Format(this.Get_Inventory_ID());
    }


    /**
     *
     * @param id
     * @param name
     * @param inventory_ID
     */
    public Item_Class (int id, String name, ArrayList<String> inventory_ID)
    {
        this.id = id;
        this.name = name;
        this.inventory_ID = inventory_ID;
        this.valid_Inventory_ID_Format = Validate_Inventory_ID_Format(this.Get_Inventory_ID());
    }


    /**
     *
     * @param id
     * @param name
     * @param inventory_ID
     * @param valid_Inventory_ID_Format
     */
    public Item_Class (int id, String name, ArrayList<String> inventory_ID, boolean valid_Inventory_ID_Format)
    {
        this.id = id;
        this.name = name;
        this.inventory_ID = inventory_ID;
        this.valid_Inventory_ID_Format = valid_Inventory_ID_Format;
    }





    /**
     *
     * @return
     */
    public int Get_ItemID()
    {
        return this.id;
    }


    /**
     *
     * @return
     */
    public String Get_ItemName()
    {
        return this.name;
    }


    /**
     *
     * @return
     */
    public ArrayList<String> Get_Inventory_ID()
    {
        return this.inventory_ID;
    }


    /**
     *
     * @return
     */
    public boolean Get_Valid_Inventory_ID_Format()
    {
        return this.valid_Inventory_ID_Format;
    }




    private boolean Validate_Inventory_ID_Format(ArrayList<String> id_List)
    {
        // Validate: ArrayList is not Null.
        if (id_List == null)
        {
            return false;
        }


        // Validate: ArrayList is not empty.
        if (id_List.size() <= 0)
        {
            return false;
        }


        // Validate: Each Element in the ArrayList is not Noll, and is not Empty.
        for (String id : id_List)
        {
            if ( id == null  ||  id.isEmpty() )
            {
                return false;
            }
        }


        // Validate: Each Element doesn't use any illegal signs.
        for (String id : id_List)
        {
            String temp_String = id;

            // Check: Only BlankSpace / Whitespace
            String temp_String1 = temp_String.replaceAll("\\s+", "");

            if ( temp_String1.isEmpty() )
            {
                return false;
            }

            // Check: Only Dangerous Character
            String temp_String2 = temp_String;
            String danger_String = ".*[;'\"<>(){}%].*";

            if ( temp_String2.matches(danger_String) )
            {
                return false;
            }
        }

        return true;
    }





}







