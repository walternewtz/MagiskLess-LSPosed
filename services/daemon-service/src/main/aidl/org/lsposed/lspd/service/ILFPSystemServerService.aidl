package org.lsposed.lspd.service;

import org.lsposed.lspd.service.ILFPApplicationService;

interface ILFPSystemServerService {
    ILFPApplicationService requestApplicationService(int uid, int pid, String processName, IBinder heartBeat);
}
