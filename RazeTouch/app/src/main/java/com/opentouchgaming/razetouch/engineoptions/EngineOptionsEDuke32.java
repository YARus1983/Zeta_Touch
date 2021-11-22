package com.opentouchgaming.razetouch.engineoptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.arch.core.util.Function;

import com.opentouchgaming.androidcore.AppInfo;
import com.opentouchgaming.androidcore.AppSettings;
import com.opentouchgaming.androidcore.DebugLog;
import com.opentouchgaming.androidcore.EngineOptionsInterface;
import com.opentouchgaming.androidcore.GameEngine;
import com.opentouchgaming.androidcore.Utils;
import com.opentouchgaming.androidcore.ui.AudioOverride;
import com.opentouchgaming.razetouch.R;

import java.io.File;
import java.util.ArrayList;



public class EngineOptionsEDuke32 implements EngineOptionsInterface
{
    static DebugLog log;

    static
    {
        log = new DebugLog(DebugLog.Module.GAMEFRAGMENT, "EngineOptionsEDuke32");
    }

    Dialog dialog;
    AudioOverride audioOverride;

    int renderMode = 0; // 0=soft, 1 = gl2

    int width = 640;
    int height = 480;

    public EngineOptionsEDuke32()
    {
        audioOverride = new AudioOverride("eduke_");
    }

    @Override
    public void showDialog(final Activity activity, GameEngine engine, int version, Function<Integer, Void> update)
    {
        loadSettings();

        dialog = new Dialog(activity, R.style.MyDialog);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        dialog.setTitle("EDuke32 options");
        dialog.setContentView(R.layout.dialog_options_eduke);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        // Handles audio override
        audioOverride.linkUI(activity, dialog);

        final RadioButton gl2Radio = dialog.findViewById(R.id.gles2_radioButton);
        final RadioButton swRadio = dialog.findViewById(R.id.software_radioButton);

        if (renderMode == 0)
            swRadio.setChecked(true);
        else if(renderMode == 2)
            gl2Radio.setChecked(true);


        final EditText widthEdit = dialog.findViewById(R.id.width_editText);
        final EditText heightEdit = dialog.findViewById(R.id.height_editText);

        widthEdit.setText(String.valueOf(width));
        heightEdit.setText(String.valueOf(height));


        dialog.setOnDismissListener(dialogInterface ->
                                    {
                                        renderMode = swRadio.isChecked() ? 0 : 2;
                                        width = Integer.decode(widthEdit.getText().toString());
                                        height = Integer.decode(heightEdit.getText().toString());
                                        saveSettings();
                                    });


        Button delete = dialog.findViewById(R.id.delete_cfg_button);
        delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                {
                    final String cfgRoot = AppInfo.getUserFiles() + "/yq2/";
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                    dialogBuilder.setMessage("Delete all Eduke32 config files?");
                    dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            //new File(file).delete();
                            log.log(DebugLog.Level.D,"cfgRoot = " + cfgRoot);
                            ArrayList<String> files = new ArrayList<>();
                            Utils.findFiles(new File(cfgRoot),"config.cfg", files);
                            for( String f: files )
                            {
                                log.log(DebugLog.Level.D,"file to delete = " + f);
                                new File(f).delete();
                            }
                        }
                    });
                    AlertDialog dialog = dialogBuilder.create();
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.show();
                }
            }
        });

        dialog.show();
    }
    private void saveSettings()
    {
        AppSettings.setIntOption(AppInfo.getContext(), "eduke_ref", renderMode);
        AppSettings.setIntOption(AppInfo.getContext(), "eduke_width", width);
        AppSettings.setIntOption(AppInfo.getContext(), "eduke_height", height);
    }

    private void loadSettings()
    {
        renderMode = AppSettings.getIntOption(AppInfo.getContext(), "eduke_ref", 2);
        width = AppSettings.getIntOption(AppInfo.getContext(), "eduke_width", 400);
        height = AppSettings.getIntOption(AppInfo.getContext(), "eduke_height", 240);
    }

    public String getArgs(int version)
    {
        loadSettings();

        if( renderMode == 0)
            return  " -screen_bpp 8 -screen_width " + width + "  -screen_height " + height +  " ";
        else
            return  " -screen_bpp 8 -screen_width $W -screen_height $H ";
    }



    @Override
    public RunInfo getRunInfo(int version) {

        RunInfo info = new RunInfo();

        info.args = getArgs(version);
        info.glesVersion = 2;
        info.useGL4ES = true;

        return info;
    }

    @Override
    public boolean hasMultiplayer()
    {
        return false;
    }

    @Override
    public void launchMultiplayer(Activity ac, GameEngine engine, int version, String mainArgs, MultiplayerCallback callback)
    {

    }

    @Override
    public int audioOverrideFreq()
    {
        return audioOverride.getFreq();
    }

    @Override
    public int audioOverrideSamples()
    {
        return audioOverride.getSamples();
    }

    @Override
    public int audioOverrideBackend()
    {
        return audioOverride.getBackend();
    }
}
