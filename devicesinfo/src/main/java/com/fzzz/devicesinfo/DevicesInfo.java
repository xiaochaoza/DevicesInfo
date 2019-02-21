package com.fzzz.devicesinfo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

/**
 * Created by Shen Chao
 * Created by 2019-02-21
 */
public class DevicesInfo {
    private DevicesInfo devicesInfo = new DevicesInfo();
    private Activity activity;
    private BatteryManager batteryManager;
    private TelephonyManager telephonyManager;
    private WifiManager wifiManager;
    private LocationManager locationManager;
    private CustomLocationListener locationListener;
    private double longitude, latitude;
    private static final String CHINA_MOVE_ONE = "46000";//中国移动
    private static final String CHINA_MOVE_TWO = "46002";
    private static final String CHINA_MOVE_THREE = "46007";
    private static final String CHINA_MOVE_FOUR = "46008";
    private static final String CHINA_UNICOM_ONE = "46001";//中国联通
    private static final String CHINA_UNICOM_TWO = "46006";//中国联通
    private static final String CHINA_UNICOM_THREE = "46009";//中国联通
    private static final String CHINA_TELECOM_ONE = "46003";//中国电信
    private static final String CHINA_TELECOM_TWO = "46005";//中国电信
    private static final String CHINA_TELECOM_THREE = "46011";//中国电信

    private DevicesInfo() {

    }

    public DevicesInfo getInstance() {
        return devicesInfo;
    }

    public void init(Activity activity) {
        this.activity = activity;
        //获取电池服务
        batteryManager = (BatteryManager) activity.getSystemService(Context.BATTERY_SERVICE);
        telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new CustomLocationListener();
    }

    /**
     * 获取屏幕宽高
     *
     * @return 屏幕宽高，单位px
     */
    private String getScreen() {
        String screen = "";
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;
        return heightPixels + "*" + widthPixels;
    }

    /**
     * 获取屏幕亮度
     *
     * @return 屏幕亮度值，最大值为255
     */
    private String getBright() {
        String bright = "";
        try {
            bright = Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS) + "";
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return bright;
    }

    /**
     * 获取当前语言
     *
     * @return 语言代码，例如ZH US
     */
    private String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取运行时间
     *
     * @return 系统开机后的运行时间，默认毫秒
     */
    private String getRunTime() {
        return System.currentTimeMillis() - SystemClock.elapsedRealtime() + "";
    }

    /**
     * 获取开机时间
     *
     * @return 转换为长时间格式
     */
    private String getBootTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime();
        Date date = new Date(bootTime);
        return simpleDateFormat.format(date);
    }

    /**
     * 获取系统时间
     *
     * @return 系统时间，转换为长时间格式
     */
    private String getCurrTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    /**
     * 获取设备名称
     *
     * @return 设备名称
     */
    private String getHardware() {
        return Build.DEVICE;
    }

    /**
     * 获取WIFI MAC 地址
     *
     * @return WIFI MAC 地址
     */
    private String getWifiMac() {
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        return connectionInfo.getMacAddress();
    }

    /**
     * 获取WIFI MAC 地址
     *
     * @return WIFI MAC 地址
     */
    private String getWifiName() {
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        return connectionInfo.getSSID();
    }

    /**
     * 获取WIFI MAC 地址
     *
     * @return WIFI MAC 地址
     */
    private String getPseudoUniqueID() {
        return Build.SERIAL;
    }

    /**
     * 获取WIFI MAC 地址
     *
     * @return WIFI MAC 地址
     */
    private String getAndroidId() {
        //androidID 手机恢复出厂设置这个值会变，root手机后这个值可以更改
        return Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * 获取IMSI
     *
     * @return IMSI码
     */
    private String getIMSI() {
        String iMSI = "";
        String[] PERMISSIONS_READ_PHONE_STATE = {"android.permission.READ_PHONE_STATE"};
        if (telephonyManager != null) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, PERMISSIONS_READ_PHONE_STATE, 1);
            } else {
                iMSI = telephonyManager.getSubscriberId();
            }
        }
        return iMSI;
    }

    /**
     * 获取CPU内核数量
     *
     * @return CPU内核数量
     */
    private int getCpuCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * 获取CPU类型
     *
     * @return CPU类型，例如arm64位
     */
    private String getCpuAbi() {
//        return Build.CPU_ABI;
//        An ordered list of ABIs supported by this device. The most preferred ABI is the first element in the list.
        //之前方法已过期，新方法返回数组，一般都取第一个值
        String[] supportedAbis = Build.SUPPORTED_ABIS;
        return supportedAbis[0];
    }

    /**
     * 获取当前电量
     *
     * @return 当前电量，默认返回百分比
     */
    private String getBatLvl() {
        //BATTERY_PROPERTY_CAPACITY 获取当前电量占总电量的百分比，无小数
        int intCapacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        return intCapacity + "%";
    }

    /**
     * 获取电池状态
     *
     * @return 电池状态
     */
    private String getBatStat() {
        int intStatus = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS);
        String result = "未知状态";
        switch (intStatus) {

            case BatteryManager.BATTERY_STATUS_UNKNOWN:
                result = "未知状态";
                break;
            case BatteryManager.BATTERY_STATUS_CHARGING:
                result = "充电中";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                result = "未充电";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                result = "电池已充满";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                result = "放电中";
                break;
        }
        return result;
    }

    /**
     * 获取内存大小
     *
     * @return 存储设备大小，单位GB
     */
    private String getSDSize() {
        String size = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File path = Environment.getExternalStorageDirectory();
            StatFs statFs = new StatFs(path.getPath());
            // 获取块的数量
            long blockSize = statFs.getBlockSizeLong();
            // 获取一共有多少块
            long totalBlocks = statFs.getBlockCountLong();
            // 可以活动的块
            long avaibleBlocks = statFs.getAvailableBlocksLong();
            size = Formatter.formatFileSize(activity, blockSize * totalBlocks);
        }
        return size;
    }

    /**
     * 获取手机号码
     *
     * @return 手机号码
     */
    private String getPhoneNumber() {
        String[] PERMISSIONS_READ_PHONE_NUMBERS = {"android.permission.READ_PHONE_NUMBERS"};
        String phoneNumber = "";
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_READ_PHONE_NUMBERS, 1);
        } else {
            phoneNumber = telephonyManager.getLine1Number() + "";
        }
        if (!TextUtils.isEmpty(phoneNumber)) {
            phoneNumber = phoneNumber.substring(3);
        }
        return phoneNumber;
    }

    /**
     * 获取系统版本
     *
     * @return 系统版本
     */
    private String getOsVersion() {
        return Build.VERSION.SDK + " " + Build.VERSION.RELEASE;
    }

    /**
     * 获取版本名称
     *
     * @return 版本名称
     */
    private String getVersionName() {
        PackageManager pm = activity.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo(activity.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo.versionName;
    }

    /**
     * 获取手机品牌
     *
     * @return 手机品牌信息
     */
    public String getDeviceBrand() {
        return Build.BRAND;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号信息
     */
    public String getDeviceType() {
        return Build.MODEL;
    }

    /**
     * 获取设备mac地址
     *
     * @return 设备MAC地址，此地址和设置中显示的一样
     */
    public String getWlanMacAddress() {
        try {
            String interfaceName = "wlan0";
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)) {
                    continue;
                }

                byte[] mac = intf.getHardwareAddress();
                if (mac == null) {
                    return "";
                }

                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取ip地址
     *
     * @return 当前外网IP地址
     */
    public String getIpAddressString() {
        try {
            for (Enumeration<NetworkInterface> enNetI = NetworkInterface
                    .getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
                NetworkInterface netI = enNetI.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = netI
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取IMEI
     *
     * @return IMEI码
     */
    public String getIMEI() {
        String imei = "";
        String[] PERMISSIONS_READ_PHONE_STATE = {"android.permission.READ_PHONE_STATE"};
        if (telephonyManager != null) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, PERMISSIONS_READ_PHONE_STATE, 1);
            } else {
                imei = telephonyManager.getDeviceId();
            }
        }
        return imei;
    }

    /**
     * 获取运营商
     *
     * @return 根据编码返回运营商名称
     */
    public String getCarrier() {
        String res = "";
        if (telephonyManager != null) {
            String operator = telephonyManager.getSimOperator();
            if (operator != null) {

                if (operator.equals(CHINA_MOVE_ONE) || operator.equals(CHINA_MOVE_TWO) || operator.equals(CHINA_MOVE_THREE) || operator.equals(CHINA_MOVE_FOUR)) {
                    //中国移动
                    res = "中国移动";
                } else if (operator.equals(CHINA_UNICOM_ONE) || operator.equals(CHINA_UNICOM_TWO) || operator.equals(CHINA_UNICOM_THREE)) {
                    //中国联通
                    res = "中国联通";
                } else if (operator.equals(CHINA_TELECOM_ONE) || operator.equals(CHINA_TELECOM_TWO) || operator.equals(CHINA_TELECOM_THREE)) {
                    //中国电信
                    res = "中国电信";
                }
            }
        }
        return res;
    }

    /**
     * 获取网络类型
     *
     * @return 网络类型，例如WIFI, 4G
     */
    public String getNetWorkType() {
        //获取系统的网络服务
        ConnectivityManager connManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        //如果当前没有网络
        if (null == connManager)
            return "";

        //获取当前网络类型，如果为空，返回无网络
        NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();
        if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
            return "";
        }

        // 判断是不是连接的是不是wifi
        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != wifiInfo) {
            NetworkInfo.State state = wifiInfo.getState();
            if (null != state)
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    return "wifi";
                }
        }

        // 如果不是wifi，则判断当前连接的是运营商的哪种网络2g、3g、4g等
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (null != networkInfo) {
            NetworkInfo.State state = networkInfo.getState();
            String strSubTypeName = networkInfo.getSubtypeName();
            if (null != state)
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    switch (activeNetInfo.getSubtype()) {
                        //如果是2g类型
                        case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
                        case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
                        case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            return "2g";
                        //如果是3g类型
                        case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            return "3g";
                        //如果是4g类型
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            return "4g";
                        default:
                            //中国移动 联通 电信 三种3G制式
                            if (strSubTypeName.equalsIgnoreCase("TD-SCDMA") || strSubTypeName.equalsIgnoreCase("WCDMA") || strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                                return "3g";
                            } else {
                                return "mobile";
                            }
                    }
                }
            return "";
        }
        return "";
    }

    /**
     * 判断手机是否root
     *
     * @return 是否root布尔值
     */
    public boolean isRoot() {
        String binPath = "/system/bin/su";
        String xBinPath = "/system/xbin/su";

        if (new File(binPath).exists() && isCanExecute(binPath)) {
            return true;
        }
        if (new File(xBinPath).exists() && isCanExecute(xBinPath)) {
            return true;
        }
        return false;
    }

    private static boolean isCanExecute(String filePath) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("ls -l " + filePath);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str = in.readLine();
            if (str != null && str.length() >= 4) {
                char flag = str.charAt(3);
                if (flag == 's' || flag == 'x')
                    return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    /**
     * 校验并获取定位信息
     */
    private String getLBSInfo() {
        //获取用户手机的位置服务
        String brand = Build.BRAND;
        //针对vivo做权限处理
        if ("vivo".equals(brand)) {
            LocationProvider provider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
            if (provider != null) {
                //已授权
                getLBS();
            }
        } else {
            //其他机型处理
            getLBS();
        }
        return longitude + "*" + latitude;
    }

    private void getLBS() {
        Location location = null;
        String providerResult = "";
        List<String> providers = locationManager.getProviders(true);
        locationListener = new CustomLocationListener();
        String[] PERMISSIONS_READ_PHONE_STATE = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"};

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_READ_PHONE_STATE, 1);
        } else {
            for (String provider : providers) {

                Location nextLocation = locationManager.getLastKnownLocation(provider);
                if (nextLocation == null) {
                    continue;
                }
                if (location == null || nextLocation.getAccuracy() < location.getAccuracy()) {
                    location = nextLocation;
                    providerResult = provider;
                }
            }
            if (location != null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            } else {
                locationManager.requestLocationUpdates(providerResult, 0, 0, locationListener);
            }
        }

    }

    private class CustomLocationListener implements LocationListener {

        //当位置发生变化的时候 调用.
        @Override
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }

        //位置提供者状态变化的时候 调用
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        //当某个位置提供者 被启用
        @Override
        public void onProviderEnabled(String provider) {

        }

        //当某个位置提供者 被禁用
        @Override
        public void onProviderDisabled(String provider) {

        }

    }
}
