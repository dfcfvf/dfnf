package com.ef.vm.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.ef.vm.start.models.AppInfo;
import com.ef.vm.start.models.AppInfoLite;
import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.remote.InstallResult;
import com.lody.virtual.remote.InstalledAppInfo;

import java.util.List;

/**
 * Created by admin on 2017-07-28-0028.
 */

public class VMUtils {

    /**
     * 判断是否安装
     *
     * @param packageName
     * @return
     */
    public static boolean isAppInstalledAsUser(String packageName) {
        try {
            return VirtualCore.get().isAppInstalledAsUser(0, packageName);
        } catch (Exception e) {
            uninstallApp(packageName);
            return false;
        }
    }

    /**
     * 判断是否运行
     *
     * @param packageName
     * @return
     */
    public static boolean isAppRunning(String packageName) {
        return isRunning(packageName);
    }

    /**
     * 卸载App
     *
     * @param packageName
     */
    public static boolean uninstallApp(String packageName) {
        VirtualCore core = VirtualCore.get();
        try {
            return core.uninstallPackage(packageName);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 安装app
     */
    public static boolean installapp(String pkg) {
        VirtualCore core = VirtualCore.get();
        if (isAppInstalledAsUser(pkg)) {
            return true;
        }
        ApplicationInfo info = null;
        try {
            info = VirtualCore.get().getUnHookPackageManager().getApplicationInfo(pkg, 0);
            if (info == null || info.sourceDir == null) {
                return false;
            }
            InstallResult result = core.installPackage(info.sourceDir, InstallStrategy.DEPEND_SYSTEM_IF_EXIST);
            return result.isSuccess;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 安装app
     */
    public static void installapp_old(Context context, String pkg, int userId) {
        AppInfoLite infolite = getAppInfoLiteByPkg(context, pkg);
        if (infolite != null) {
            addVirtualApp(infolite);
        }
    }

    /**
     * 安装app
     */
    public static void installapp(String pkg, int userId) {
        VirtualCore core = VirtualCore.get();
        if (core.isAppInstalledAsUser(userId, pkg)) {
            return;
        }
        ApplicationInfo info = null;
        try {
            info = VirtualCore.get().getUnHookPackageManager().getApplicationInfo(pkg, 0);
        } catch (PackageManager.NameNotFoundException e) {
            // Ignore
        }
        if (info == null || info.sourceDir == null) {
            return;
        }
        if (userId == 0) {
            core.installPackage(info.sourceDir, InstallStrategy.DEPEND_SYSTEM_IF_EXIST);
        } else {
            core.installPackageAsUser(userId, pkg);
        }
    }

    /**
     * 安装appold
     */
    public static void installapps_old(Context context, List<String> list, int userId) {
        for (String packageName : list) {
            AppInfoLite infolite = getAppInfoLiteByPkg(context, packageName);
            if (infolite != null) {
                addVirtualApp(infolite);
            }
        }
    }

    /**
     * 安装app
     */
    public static void installapps(List<String> list, int userId) {
        VirtualCore core = VirtualCore.get();
        for (String packageName : list) {
            if (core.isAppInstalledAsUser(userId, packageName)) {
                continue;
            }
            ApplicationInfo info = null;
            try {
                info = VirtualCore.get().getUnHookPackageManager().getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                // Ignore
            }
            if (info == null || info.sourceDir == null) {
                continue;
            }
            if (userId == 0) {
                core.installPackage(info.sourceDir, InstallStrategy.DEPEND_SYSTEM_IF_EXIST);
            } else {
                core.installPackageAsUser(userId, packageName);
            }
        }
    }

    public static boolean isRunning(String pkg) {
        VirtualCore core = VirtualCore.get();
        if (core.isAppInstalledAsUser(0, pkg)) {
            return core.isAppRunning(pkg, 0);
        } else {
            return false;
        }
    }

    public static AppInfoLite getAppInfoLiteByPkg(Context context, String packagename) {
        AppInfoLite infolite = null;
        try {
            PackageInfo pkg = context.getPackageManager().getPackageInfo(packagename, 0);
            ApplicationInfo ai = pkg.applicationInfo;
            PackageManager pm = context.getPackageManager();
            String path = ai.publicSourceDir != null ? ai.publicSourceDir : ai.sourceDir;
            AppInfo info = new AppInfo();
            info.packageName = pkg.packageName;
            info.fastOpen = true;
            info.path = path;
            info.icon = ai.loadIcon(pm);
            info.name = ai.loadLabel(pm);
            InstalledAppInfo installedAppInfo = VirtualCore.get().getInstalledAppInfo(pkg.packageName, 0);
            if (installedAppInfo != null) {
                info.cloneCount = installedAppInfo.getInstalledUsers().length;
            }
            infolite = new AppInfoLite(info.packageName, info.path, info.fastOpen);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return infolite;
    }

    public static InstallResult addVirtualApp(AppInfoLite info) {
        if (VirtualCore.get().isAppInstalled(info.packageName)) {
            return null;
        }
        int flags = InstallStrategy.COMPARE_VERSION | InstallStrategy.SKIP_DEX_OPT;
        if (info.fastOpen) {
            flags |= InstallStrategy.DEPEND_SYSTEM_IF_EXIST;
        }
        return VirtualCore.get().installPackage(info.path, flags);
    }

    /**
     * uninstall QQ
     * yekh 2017-10-2
     */
    public static void uninstallQQ() {
        Log.e("home", "uninstall QQ");
        try {
            VirtualCore.get().uninstallPackage("com.tencent.mobileqq");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
