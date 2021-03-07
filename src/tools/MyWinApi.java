package tools;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import jforth.JForth;

import java.awt.*;

import static com.sun.jna.platform.win32.WinUser.*;

public class MyWinApi
{
    private static final Wincon Kernel = Kernel32.INSTANCE;
    private static final User32 User = User32.INSTANCE;
    private static final HWND notopHwnd = new HWND(Pointer.createConstant(-2));
    private static final HWND noHwnd = new HWND(Pointer.createConstant(-1));
    private static final HWND topHwnd = new HWND(Pointer.createConstant(0));

    private static HWND getConsoleHWND() throws Exception
    {
        HWND hwnd = Kernel.GetConsoleWindow();
        if (hwnd == null) {
            throw new Exception("no HWND");
        }
        return hwnd;
    }

    public static void SetConsoleToFG () throws Exception {
        HWND hwnd = getConsoleHWND();
        User.SetWindowPos (hwnd, noHwnd, 0,0,0,0,
                WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE);
    }

    public static void SetConsolePos (Point p) throws Exception {
        HWND hwnd = getConsoleHWND();
        User.SetWindowPos (hwnd, notopHwnd, p.x, p.y,0,0, WinUser.SWP_NOSIZE);
    }

    public static void SetConsoleSize (Point p) throws Exception {
        HWND hwnd = getConsoleHWND();
        User.SetWindowPos (hwnd, notopHwnd, 0,0, p.x, p.y, WinUser.SWP_NOMOVE);
    }

    public static void SetConsoleFullScreen() throws Exception {
        HWND hwnd = getConsoleHWND();
        User.SendMessage(hwnd, WinUser.WM_SYSKEYDOWN,
                  new WinDef.WPARAM(0x0d),
                  new WinDef.LPARAM(0x20000000));
    }

    public static void showWnd (long show) throws Exception {
        HWND hwnd = getConsoleHWND();
        User.ShowWindow(hwnd, show == JForth.TRUE ? SW_SHOW : SW_HIDE);
    }
}
