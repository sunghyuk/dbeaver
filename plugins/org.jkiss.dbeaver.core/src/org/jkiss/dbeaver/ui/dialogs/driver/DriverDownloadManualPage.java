/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2015 Serge Rieder (serge@jkiss.org)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 2)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jkiss.dbeaver.ui.dialogs.driver;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.jkiss.dbeaver.core.DBeaverUI;
import org.jkiss.dbeaver.registry.DriverDescriptor;
import org.jkiss.dbeaver.registry.DriverFileSource;
import org.jkiss.dbeaver.runtime.RuntimeUtils;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.preferences.PrefPageDrivers;
import org.jkiss.utils.CommonUtils;

import java.util.List;

class DriverDownloadManualPage extends DriverDownloadPage {

    private DriverFileSource fileSource;

    DriverDownloadManualPage() {
        super("Configure driver files", "Download driver files", null);
        setPageComplete(false);
    }

    @Override
    public void createControl(Composite parent) {
        final DriverDescriptor driver = getWizard().getDriver();
        fileSource = driver.getDriverFileSources().get(0);

        setMessage("Download & configure " + driver.getFullName() + " driver files");

        initializeDialogUnits(parent);

        Composite composite = UIUtils.createPlaceholder(parent, 1);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        StringBuilder message = new StringBuilder();
        message.append("Driver ").append(driver.getFullName());
        message.append("files missing.\n\n" +
            "According to vendor policy this driver isn''t publicly available and you have to download it manually from vendor''s web site.\n\n" +
            "After successful driver download you will need to add JAR files in DBeaver libraries list.");
        if (!CommonUtils.isEmpty(fileSource.getInstructions())) {
            message.append("\n").append(fileSource.getInstructions());
        }
        message.append("\n\nFiles to download: ");
        for (DriverFileSource.FileInfo file : fileSource.getFiles()) {
            message.append("\n\t").append(file.getName());
        }

        Text infoText = new Text(composite, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
        infoText.setText(message.toString());
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 200;
        infoText.setLayoutData(gd);

        //UIUtils.createHorizontalLine(composite);
        UIUtils.createPlaceholder(composite, 1).setLayoutData(new GridData(GridData.FILL_BOTH));

        {
            Composite linksGroup = UIUtils.createPlaceholder(composite, 2);
            ((GridLayout)linksGroup.getLayout()).makeColumnsEqualWidth = true;
            linksGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            // Vendor site
            if (!CommonUtils.isEmpty(driver.getWebURL())) {
                Link link = UIUtils.createLink(
                    linksGroup,
                    "<a>Vendor's website</a>",
                    new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            RuntimeUtils.openWebBrowser(driver.getWebURL());
                        }
                    });
                link.setToolTipText(driver.getWebURL());
                link.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_BEGINNING));
            } else {
                UIUtils.createPlaceholder(linksGroup, 1).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            }

            Link link = UIUtils.createLink(
                linksGroup,
                "<a>Download configuration</a>",
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        UIUtils.showPreferencesFor(
                            DBeaverUI.getActiveWorkbenchShell(),
                            null,
                            PrefPageDrivers.PAGE_ID);
                    }
                });
            link.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END));
        }

        setControl(composite);
    }


    @Override
    void performFinish() {
        UIUtils.runInDetachedUI(getShell(), new Runnable() {
            @Override
            public void run() {
                RuntimeUtils.openWebBrowser(fileSource.getUrl());
            }
        });
        DriverEditDialog dialog = new DriverEditDialog(null, getWizard().getDriver());
        dialog.open();
    }

}