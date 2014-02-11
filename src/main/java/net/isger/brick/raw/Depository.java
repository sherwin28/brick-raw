package net.isger.brick.raw;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import net.isger.brick.util.Files;
import net.isger.brick.util.Formats;
import net.isger.brick.util.hitcher.Director;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 保管仓
 * 
 * @author issing
 * 
 */
public class Depository extends Director {

    private static final Logger LOG;

    private static final Object LOCKED = new Object();

    private static final Depository DEPOS = new Depository();

    /** 分隔标记 */
    private static final String TOKEN_SEPARETOR = ":";

    /** 仓库包属性键 */
    private static final String KEY_DEPOTS = "brick.raw.depots";

    /** 仓库默认路径 */
    private static final String DEPOT_PATH = "net/isger/brick/raw/depot";

    /** 仓库配置文件 */
    private static final String DEPOTS_PROPS = "depots.properties";

    /** 仓库类配置 */
    private static final String PROP_DEPOT_CLASSES = "depot.classes";

    /** 可写仓库集合 */
    private Hashtable<Class<?>, Depot> writeDepots;

    /** 可读仓库集合 */
    private Hashtable<Class<?>, Depot> readDepots;

    static {
        LOG = LoggerFactory.getLogger(Depository.class);
    }

    private Depository() {
        writeDepots = new Hashtable<Class<?>, Depot>();
        readDepots = new Hashtable<Class<?>, Depot>();
    }

    protected String directHitchPath() {
        return directHitchPath(KEY_DEPOTS, DEPOT_PATH);
    }

    protected void directAttach(String path) {
        ClassLoader loader = Depository.class.getClassLoader();
        Enumeration<URL> urlEnum;
        try {
            path = Formats.toPath(path, DEPOTS_PROPS);
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

    @SuppressWarnings("unchecked")
    protected void directSanity() {
        readDepots = (Hashtable<Class<?>, Depot>) writeDepots.clone();
        // 挂载默认资源
        mount();
    }

    /**
     * 加载仓库（根据资源配置文件）
     * 
     * @param url
     */
    private void loadDepots(URL url) {
        // 注册配置仓库
        Depot depot = null;
        Properties props = loadProps(url);
        props.getProperty(PROP_DEPOT_CLASSES);
        StringTokenizer depots = getDepots(props);
        while (depots.hasMoreElements()) {
            try {
                depot = newDepot(depots.nextElement().toString());
            } catch (Exception e) {
                LOG.error("Failure to loading depot [{}].",
                        depots.nextElement(), e);
                continue;
            }
            addDepot(depot);
        }
    }

    /**
     * 加载配置
     * 
     * @param url
     * @return
     */
    private Properties loadProps(URL url) {
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
     * 获取指定配置仓库
     * 
     * @param props
     * @return
     */
    private StringTokenizer getDepots(Properties props) {
        return new StringTokenizer(props.getProperty(PROP_DEPOT_CLASSES, ""),
                TOKEN_SEPARETOR);
    }

    /**
     * 创建仓库
     * 
     * @param name
     * @return
     * @throws Exception
     */
    private Depot newDepot(String name) throws Exception {
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

    private void addDepot(Depot depot) {
        Class<?> clazz = depot.getClass();
        Depot oldDepot = writeDepots.put(clazz, depot);
        if (oldDepot != null) {
            LOG.warn("Multiple Binding depot {}", clazz);
        } else {
            LOG.info("Binding depot {}", depot);
        }
    }

    /**
     * 挂载默认资源
     * 
     */
    private void mount() {
        ClassLoader loader = this.getClass().getClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        URL url = loader.getResource("/");
        try {
            mount(url);
        } catch (Exception e) {
            mount("./");
        }
        final String separator = getSystemProperty("path.separator",
                TOKEN_SEPARETOR);
        StringTokenizer pathToken = new StringTokenizer(getSystemProperty(
                "java.class.path", ""), separator);
        mount(pathToken);
        // pathToken = new StringTokenizer(getSystemProperty("java.lib.path",
        // ""),
        // separator);
        // mount(pathToken);
    }

    private String getSystemProperty(final String key, final String def) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty(key, def);
            }
        });
    }

    /**
     * 挂载资源
     * 
     * @param pathToken
     */
    private void mount(StringTokenizer pathToken) {
        while (pathToken.hasMoreElements()) {
            mount(pathToken.nextElement().toString());
        }
    }

    /**
     * 挂载资源
     * 
     * @param url
     */
    private void mount(URL url) {
        for (Depot depot : readDepots.values()) {
            if (depot.isSupport(Depot.LAB_CLASSPATH)) {
                depot.mount(url);
            }
        }
    }

    /**
     * 挂载资源
     * 
     * @param path
     */
    private void mount(String path) {
        for (Depot depot : readDepots.values()) {
            if (depot.isSupport(Depot.LAB_CLASSPATH)) {
                depot.mount(path);
            }
        }
    }

    /**
     * 获取资源保管仓
     * 
     * @return
     */
    public static Depository getDepository() {
        return canonicalize(DEPOS);
    }

    /**
     * 注册仓库
     * 
     * @param name
     * @param depot
     * @return
     */
    @SuppressWarnings("unchecked")
    public void add(Depot depot) {
        addDepot(depot);
        readDepots = (Hashtable<Class<?>, Depot>) writeDepots.clone();
    }

    /**
     * 挂载指定路径资源
     * 
     * @param path
     */
    public static void mount(String label, String path) {
        for (Depot depot : getDepots()) {
            if (depot.isSupport(label)) {
                depot.mount(path);
            }
        }
    }

    /**
     * 寻觅种子
     * 
     * @param label
     * @param info
     * @return
     */
    public static Seed seek(String label, String info) {
        Seed seed = null;
        for (Depot depot : getDepots()) {
            if (depot.isSupport(label)) {
                if ((seed = depot.seek(info)) != null) {
                    break;
                }
            }
        }
        return seed;
    }

    /**
     * 包装种子
     * 
     * @param label
     * @param info
     * @return
     */
    public static Artifact wrap(String label, String info) {
        Artifact artifact = null;
        Seed seed = null;
        for (Depot depot : getDepots()) {
            if (depot.isSupport(label)) {
                seed = depot.seek(info);
                if (seed == null) {
                    continue;
                }
                artifact = depot.wrap(seed);
                if (artifact == null) {
                    artifact = wrap(label, seed);
                    if (artifact != null) {
                        break;
                    }
                    continue;
                }
                break;
            }
        }
        return artifact;
    }

    /**
     * 包装种子
     * 
     * @param wrapLabel
     * @param seekLabel
     * @param info
     * @return
     */
    public static Artifact wrap(String wrapLabel, String seekLabel, String info) {
        Artifact artifact = null;
        Seed seed = null;
        for (Depot depot : getDepots()) {
            if (depot.isSupport(seekLabel)) {
                seed = depot.seek(info);
                if (seed == null) {
                    continue;
                }
                artifact = wrap(wrapLabel, seed);
                if (artifact != null) {
                    break;
                }
            }
        }
        return artifact;
    }

    /**
     * 包装种子
     * 
     * @param label
     * @param seed
     * @return
     */
    public static Artifact wrap(String label, Seed seed) {
        Artifact artifact = null;
        for (Depot depot : getDepots()) {
            if (depot.isSupport(label)) {
                if ((artifact = depot.wrap(seed)) != null) {
                    break;
                }
            }
        }
        return artifact;
    }

    /**
     * 卸载指定路径资源
     * 
     * @param path
     */
    public static void unmount(String label, String path) {
        for (Depot depot : getDepots()) {
            if (depot.isSupport(label)) {
                depot.unmount(path);
            }
        }
    }

    /**
     * 获取所有仓库
     * 
     * @return
     */
    private static Collection<Depot> getDepots() {
        Collection<Depot> depots = null;
        synchronized (LOCKED) {
            depots = getDepository().readDepots.values();
        }
        return depots;
    }

}
