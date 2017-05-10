/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package libcore.tzdata.update_test_app2.installupdatetestapp;

import android.app.Activity;
import android.app.timezone.Callback;
import android.app.timezone.RulesManager;
import android.app.timezone.RulesState;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity implements View.OnClickListener {

    private EditText contentPathEditText;
    private TextView logView;

    private RulesManager rulesManager;
    private Callback callback;
    private ExecutorService executor;
    private CheckBox successCheckbox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button triggerInstallButton = (Button) findViewById(R.id.trigger_install_button);
        triggerInstallButton.setOnClickListener(this);
        Button triggerUninstallButton = (Button) findViewById(R.id.trigger_uninstall_button);
        triggerUninstallButton.setOnClickListener(this);
        Button triggerCheckCompleteButton = (Button) findViewById(R.id.trigger_nothing);
        triggerCheckCompleteButton.setOnClickListener(this);
        successCheckbox = (CheckBox) findViewById(R.id.success_checkbox);

        Button getRulesStateButton = (Button) findViewById(R.id.get_rules_state_button);
        getRulesStateButton.setOnClickListener(this);

        contentPathEditText = (EditText) findViewById(R.id.content_path);
        logView = (TextView) findViewById(R.id.log);
        executor = Executors.newFixedThreadPool(1);
        rulesManager = (RulesManager) getSystemService("timezone");
        callback = new Callback() {
            @Override
            public void onFinished(int status) {
                logString("Operation finished. Status=" + status);
            }
        };
    }

    private abstract class MyAsyncTask extends AsyncTask<Void, String, Void> {
        @Override
        protected void onProgressUpdate(String... values) {
            for (String message : values) {
                addToLog(message, null);
            }
        }
    }

    @Override
    public void onClick(View v) {
        MyAsyncTask task;
        if (v.getId() == R.id.trigger_install_button) {
            final String contentPath = contentPathEditText.getText().toString();
            final File contentFile = new File(contentPath);

            // Convert the contentFile to a ParcelFileDescriptor.
            final ParcelFileDescriptor distroFileDescriptor =
                    createParcelFileDescriptor(contentFile);
            if (distroFileDescriptor == null) {
                return;
            }

            Callback callback = new Callback() {
                @Override
                public void onFinished(int status) {
                    logString("onFinished: Finished install: " + status);

                    // TODO(nfuller): Can this be closed sooner?
                    try {
                        distroFileDescriptor.close();
                    } catch (IOException e) {
                        logString("Unable to close ParcelFileDescriptor for " + contentFile + ": "
                                + exceptionToString(e));
                    }
                }
            };

            task = new MyAsyncTask() {
                @Override
                protected Void doInBackground(Void... params) {
                    if (!contentFile.exists()) {
                        publishProgress("Error: " +  contentFile + " does not exist.");
                        return null;
                    }
                    try {
                        int requestStatus = rulesManager.requestInstall(
                                distroFileDescriptor, null /* checkToken */, callback);
                        publishProgress("Request sent:" + requestStatus);
                    } catch (Exception e) {
                        publishProgress("Error", exceptionToString(e));
                    }
                    return null;
               }
            };
        } else if (v.getId() == R.id.trigger_uninstall_button) {
            task = new MyAsyncTask() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        int requestStatus = rulesManager.requestUninstall(
                                null /* checkToken */, callback);
                        publishProgress("Request sent:" + requestStatus);
                    } catch (Exception e) {
                        publishProgress("Error", exceptionToString(e));
                    }
                    return null;
                }
            };
        } else if (v.getId() == R.id.trigger_nothing) {
            final boolean success = successCheckbox.isChecked();
            task = new MyAsyncTask() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        rulesManager.requestNothing(null /* checkToken */, success);
                        publishProgress("Request sent");
                    } catch (Exception e) {
                        publishProgress("Error", exceptionToString(e));
                    }
                    return null;
                }
            };
        } else if (v.getId() == R.id.get_rules_state_button) {
            task = new MyAsyncTask() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        RulesState rulesState = rulesManager.getRulesState();
                        publishProgress("Rules state: " + rulesState);
                    } catch (Exception e) {
                        publishProgress("Error", exceptionToString(e));
                    }
                    return null;
                }
            };
        } else {
            addToLog("Unknown button", null);
            return;
        }
        task.executeOnExecutor(executor);
    }

    private ParcelFileDescriptor createParcelFileDescriptor(File contentFile) {
        try {
            return ParcelFileDescriptor.open(contentFile, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (FileNotFoundException e) {
            logString("Unable to create ParcelFileDescriptor from " + contentFile + ": "
                    + exceptionToString(e));
            return null;
        }
    }

    private void addToLog(String message, Exception e) {
        logString(message);
        if (e != null) {
            String text = exceptionToString(e);
            logString(text);
        }
    }

    private void logString(String value) {
        logView.append(new Date() + " " + value + "\n");
        int scrollAmount =
                logView.getLayout().getLineTop(logView.getLineCount()) - logView.getHeight();
        logView.scrollTo(0, scrollAmount);
    }

    private static String exceptionToString(Exception e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.getBuffer().toString();
    }
}
