package org.lsposed.lspd.service;

import org.lsposed.lspd.service.ILFPApplicationService;

interface ILFPosedService {
    ILFPApplicationService requestApplicationService(int uid, int pid, String processName, IBinder heartBeat);

    oneway void dispatchSystemServerContext(in IBinder activityThread, in IBinder activityToken, String api);

    boolean preStartManager(String pkgName, in Intent intent);
}
