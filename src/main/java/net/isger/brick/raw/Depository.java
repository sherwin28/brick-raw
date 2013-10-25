package net.isger.brick.raw;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;

import net.isger.brick.util.Files;
import net.isger.brick.util.Formats;
import net.isger.brick.util.Hitchers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 保管仓
 * 
 * @author issing
 * 
 */
public class Depository {

    private static final Logger LOG;

    private static final Object LOCKED = new Object();

    private static final Depository DEPOS = new Depository();

    /** 仓库包属性名 */
    private static final String PROP_DEPOTS = "brick.raw.depots";

    /** 仓库默认路径 */
    private static final String HITCH_PATH = "net/isger/brick/raw/depot";

    /** 仓库分隔标记 */
    private static final String TOKEN_DEPOT = ":";

    /** 仓库包配置 */
    private static final String DEPOTS_IRC = "depots.irc";

    private static final int UNINITIALIZED = 0;

    private static final int INITIALIZING = 1;

    private static final int INITSUCCESS = 2;

    // private static final int INITFAILURE = 3;

    /** 初始化状态 */
    private int initial;

    /** 可写仓库集合 */
    private Hashtable<String, Depot> writeDepots;

    /** 可读仓库集合 */
    private Hashtable<String, Depot> readDepots;

    static {
        LOG = LoggerFactory.getLogger(Depository.class);
    }

    private Depository() {
        initial = UNINITIALIZED;
        writeDepots = new Hashtable<String, Depot>();
        readDepots = new Hashtable<String, Depot>();
        initial();
    }

    /**
     * 初始化保管仓
     * 
     */
    @SuppressWarnings("unchecked")
    private void initial() {
        synchronized (LOCKED) {
            if (DEPOS.initial == UNINITIALIZED) {
                DEPOS.initial = INITIALIZING;
            } else {
                LOG.warn("Exists the initialize thread.");
                return;
            }
        }
        StringTokenizer depots = getDepotTokenizer();
        while (depots.hasMoreElements()) {
            loadDepots(depots.nextElement().toString());
        }
        readDepots = (Hashtable<String, Depot>) writeDepots.clone();
        DEPOS.initial = INITSUCCESS;
    }

    /**
     * 获取仓库包
     * 
     * @return
     */
    private static StringTokenizer getDepotTokenizer() {
        // 提取仓库路径
        String depots = AccessController
                .doPrivileged(new PrivilegedAction<String>() {
                    public String run() {
                        return System.getProperty(PROP_DEPOTS, HITCH_PATH);
                    }
                });
        if (depots.indexOf(HITCH_PATH) == -1) {
            depots += TOKEN_DEPOT + HITCH_PATH;
        }
        return new StringTokenizer(depots, TOKEN_DEPOT);
    }

    /**
     * 加载仓库（根据配置路径）
     * 
     * @param path
     */
    private static void loadDepots(String path) {
        Hitchers.getHitcher(path).hitch();
        ClassLoader loader = Depository.class.getClassLoader();
        Enumeration<URL> urlEnum;
        try {
            path = Formats.toPath(path, DEPOTS_IRC);
            if (loader == null) {
                urlEnum = ClassLoader.getSystemResources(path);
            } else {
                urlEnum = loader.getResources(path);
            }
        } catch (IOException e) {
            return;
        }
        while (urlEnum.hasMoreElements()) {
            loadDepots(urlEnum.nextElement());
        }
    }

    /**
     * 加载仓库（根据资源配置文件）
     * 
     * @param url
     */
    private static void loadDepots(URL url) {
        // 注册配置仓库
        Depot depot = null;
        String name = null;
        Properties props = loadProps(url);
        for (Entry<Object, Object> prop : props.entrySet()) {
            if (DEPOS.writeDepots.get(name = (String) prop.getKey()) == null) {
                try {
                    depot = newDepot((String) prop.getValue());
                    if (depot != null) {
                        DEPOS.writeDepots.put(name, depot);
                    }
                } catch (Exception e) {
                }
            } else {
                LOG.warn("Multiple [{}] loading.", name);
            }
        }
    }

    /**
     * 加载配置
     * 
     * @param url
     * @return
     */
    private static Properties loadProps(URL url) {
        Properties props = new Properties();
        InputStream is = null;
        try {
            is = url.openStream();
            props.load(is);
        } catch (IOException e) {
        } finally {
            Files.close(is);
        }
        return props;
    }

    /**
     * 创建仓库
     * 
     * @param name
     * @return
     * @throws Exception
     */
    private static Depot newDepot(String name) throws Exception {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(name);
        } catch (ClassNotFoundException e) {
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            if (loader != null) {
                clazz = loader.loadClass(name);
            }
        }
        Depot depot = null;
        if (clazz != null && Depot.class.isAssignableFrom(clazz)) {
            depot = (Depot) clazz.newInstance();
        }
        return depot;
    }

    /**
     * 获取所有仓库
     * 
     * @return
     */
    private static Hashtable<String, Depot> getDepots() {
        Hashtable<String, Depot> depots = null;
        synchronized (LOCKED) {
            depots = DEPOS.readDepots;
        }
        return depots;
    }

    /**
     * 注册仓库
     * 
     * @param name
     * @param depot
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Depot addDepot(String name, Depot depot) {
        depot = DEPOS.writeDepots.put(name, depot);
        DEPOS.readDepots = (Hashtable<String, Depot>) DEPOS.writeDepots.clone();
        return depot;
    }

    /**
     * 获取指定仓库
     * 
     * @param name
     * @return
     */
    public static Depot getDepot(String name) {
        return getDepots().get(name);
    }

    /**
     * 挂载指定路径资源
     * 
     * @param path
     */
    public static void mount(String path) {
        for (Depot depot : getDepots().values()) {
            depot.mount(path);
        }
    }

    /**
     * 寻获种子
     * 
     * @param depotName
     * @param seedName
     * @return
     */
    public static Seed seek(String depotName, String seedName) {
        return getDepot(depotName).seek(seedName);
    }

    /**
     * 包装种子
     * 
     * @param depotName
     * @param seedName
     * @return
     */
    public static Artifact wrap(String depotName, String seedName) {
        return getDepot(depotName).wrap(seedName);
    }

    /**
     * 包装种子
     * 
     * @param depotName
     * @param seed
     * @return
     */
    public static Artifact wrap(String depotName, Seed seed) {
        return getDepot(depotName).wrap(seed);
    }

    /**
     * 卸载指定路径资源
     * 
     * @param path
     */
    public static void unmount(String path) {
        for (Depot depot : getDepots().values()) {
            depot.unmount(path);
        }
    }

}
