package com.cpl.reconciliation.tasks.utils;

import com.jcraft.jsch.*;
import org.springframework.stereotype.Service;

@Service
public class SFTPService {
    public ChannelSftp connectSftp(String sftpUser, String sftpHost, Integer sftpPort, String sftpPassword) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(sftpUser, sftpHost, sftpPort);
        session.setPassword(sftpPassword);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        Channel channel = session.openChannel("sftp");
        channel.connect();
        return (ChannelSftp) channel;
    }
}
