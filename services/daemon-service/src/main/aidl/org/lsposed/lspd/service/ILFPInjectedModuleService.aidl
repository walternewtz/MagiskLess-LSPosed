package org.lsposed.lspd.service;

import org.lsposed.lspd.service.IRemotePreferenceCallback;

interface ILFPInjectedModuleService {
    int getFrameworkPrivilege();

    Bundle requestRemotePreferences(String group, IRemotePreferenceCallback callback);

    ParcelFileDescriptor openRemoteFile(String path);

    String[] getRemoteFileList();
}
