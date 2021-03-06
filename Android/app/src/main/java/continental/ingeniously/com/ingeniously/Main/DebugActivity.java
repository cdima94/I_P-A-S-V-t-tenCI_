package continental.ingeniously.com.ingeniously.Main;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import continental.ingeniously.com.ingeniously.IO.Protocol.BluetoothIO;
import continental.ingeniously.com.ingeniously.IO.Protocol.ProtocolCommand;
import continental.ingeniously.com.ingeniously.IO.Protocol.ProtocolObserver;
import continental.ingeniously.com.ingeniously.IO.Protocol.ProtocolResponse;
import continental.ingeniously.com.ingeniously.R;

public class DebugActivity extends ActionBarActivity implements ProtocolObserver {

    private EditText commandPrompt;
    //private BluetoothAdapter mBluetoothAdapter;
    private ApplicationBrodcastReciver mReciver;
    private ArrayAdapter<String> BluetoothDevicesArrayAdapter;
    private BluetoothIO bluetoothIO;
    private TextView debugInputConsole;

    /*
    private final Handler DebugHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Codes.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    /*
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    System.out.println("Recived: " + readMessage);

                    for(int i= 0; i< msg.arg1; i++){
                        System.out.print(String.format("%02X ", readBuf[i]));
                    }

                    System.out.println("---------------------------");
                    //ShowToastMesage("Recived: " + readMessage);
                    */
                    /*
                    break;
                case Codes.CONNECTION_ESTABLISHED:
                    System.out.println("Connection established");
                    ShowToastMesage("Connection established");
                    break;
                case Codes.CONNECTION_LOST:
                    ShowToastMesage("Connection lost!!");
                    //Connection lost, so try to reconnect
                    //tryConnection();
                    break;
            }

        }
    };

*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        // Do not let screen to be turned off
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        commandPrompt = (EditText) findViewById(R.id.DebugCommand_editText);
        debugInputConsole = (TextView) findViewById(R.id.debugInputConsole);

       /* mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this, "There is no Bluetooth on this device!", Toast.LENGTH_LONG).show();
            //finish();
        }*/

        //TurnBluetoothOn();
        mReciver = new ApplicationBrodcastReciver(BluetoothDevicesArrayAdapter);
        //printPairedDevices();

        //tryConnection();
        bluetoothIO = new BluetoothIO(this,this);//, DebugHandler);
        bluetoothIO.connect();




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_debug, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Codes.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    // The bluetooth is now enabled

                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // The user declined access to bluetooth
                    ShowToastMesage("Make sure to activate Bluetooth!");
                }

                break;
        }

    }


    public  String fetchCommand(String command) {

        command = command.toLowerCase();
        String commands[] = command.split("\\;");

        for (int i=0 ; i <= commands.length-1 ; i++)
        {
            System.out.println("Commands: "+commands[i]+"\n");

            if(commands[i].startsWith("move=")) {

                String tokens[] =  commands[i].split("\\=");
                String parameterList = tokens[1];
                move(parameterList);

            }else if(commands[i].startsWith("search")){

                serachForDevices();

            }else {
                System.out.println("Undefined command: \"" + commands[i] + "\" !");
            }

        }

        return command;
    }


    private  void move(String parameterList) {

        String parameters[] = parameterList.split("\\,");
        byte leftOrRight,forwardOrBackward;
        short time;
        try {
            leftOrRight = Byte.parseByte(parameters[0]);
            forwardOrBackward = Byte.parseByte(parameters[1]);
            time = Short.parseShort(parameters[2]);
        }catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if(leftOrRight > 0 ) {
            System.out.print("Moving left: " + leftOrRight);
        }else {
            System.out.print("Moving right: " + leftOrRight);
        }

        if(forwardOrBackward > 0) {
            System.out.print(" - forward: " + forwardOrBackward);
        }else {
            System.out.print(" - backward: " + forwardOrBackward);
        }

        System.out.println(" - time:" + time);

        ProtocolCommand command = new ProtocolCommand(leftOrRight,forwardOrBackward,time);
        bluetoothIO.sendCommand(command);


    }


    private static void serachForDevices() {
        // TODO Auto-generated method stub
        System.out.println("This is where you implement the \"search\" function!");
    }

    public void SendCommand(View v){

        String commandString = commandPrompt.getText().toString();
        System.out.println(commandString);
        fetchCommand(commandString);

    }

    public void ShowToastMesage(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

  /*  public void TurnBluetoothOn() {
        // Turns the bluetooth on if it is not yet on
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, Codes.REQUEST_ENABLE_BT);
        } else {
            Toast.makeText(this, "Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }

    }
*/
    public void updateInputConsole(ProtocolResponse response){
        StringBuilder sb = new StringBuilder();
        byte no_of_sens = response.getNo_of_sensors();
        byte info[] = response.getInfo();

        sb.append("Header = "+response.getHeader());
        sb.append("\nNo of sensors = "+no_of_sens);
        sb.append("\nInfo = "+no_of_sens);

        for (int i=0; i<no_of_sens; i++){
            sb.append(info[i]+"; ");
        }

        sb.append("\nTime = "+response.getTime());
        sb.append("\nCRC = "+response.getCRC());

        debugInputConsole.setText(sb.toString());
    }

    @Override
    public void responseArrived(ProtocolResponse response) {
        updateInputConsole(response);
        byte r[] = response.getBytes();
        for (int i=0; i< r.length; i++){
            System.out.print(String.format(" %02X", r[i]));
        }
    }
}
