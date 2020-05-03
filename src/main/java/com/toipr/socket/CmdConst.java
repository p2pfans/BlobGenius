package com.toipr.socket;

public class CmdConst {
    /**
     * 命令头标志
     */
    public static final int MagicCookie = 0x54495052; //TIPR

    /**
     * 客户端服务器握手，使用Token或用户名密码确认身份权限
     */
    public static final short Cmd_Handshake = 114;
    public static final short Handshake_Empty = 30;
    public static final short Handshake_Token = 31;
    public static final short Handshake_Token_Replay = 32;

    public static final short Handshake_Auth = 35;
    public static final short Handshake_Auth_Reply = 36;

    /**
     * 数据块操作命令
     */
    public static final short Cmd_DataBlob = 168;
    public static final short DataBlob_Exists = 10;
    public static final short DataBlob_Exists_Reply = 11;

    public static final short DataBlob_Get = 20;
    public static final short DataBlob_Get_Reply = 21;
    public static final short DataBlob_GetData = 26;
    public static final short DataBlob_GetData_Reply = 27;

    public static final short DataBlob_Save = 30;
    public static final short DataBlob_Save_Reply = 31;

    public static final short DataBlob_Remove = 40;
    public static final short DataBlob_Remove_Reply = 41;
}
