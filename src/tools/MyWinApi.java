package tools;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HWND;

public class MyWinApi
{
    private static final Wincon Kernel = Kernel32.INSTANCE;
    private static final User32 User = User32.INSTANCE;
    private static final HWND noHwnd = new HWND(Pointer.createConstant(-1));

    public static boolean SetConsoleToFG () {
        HWND hwnd = Kernel.GetConsoleWindow();

        if (hwnd == null) {
            System.out.println("No Console");
        }
        else {
            return User.SetWindowPos (hwnd, noHwnd, 0,0,0,0,
                    WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE);
        }
        return false;
    }
}
