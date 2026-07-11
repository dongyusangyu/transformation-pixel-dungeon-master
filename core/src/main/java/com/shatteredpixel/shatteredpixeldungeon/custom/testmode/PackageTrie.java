package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import static java.util.Collections.*;

import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.Game;
import com.watabou.utils.Reflection;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PackageTrie {

    /** package of core game files (for example com.shatteredpixel.shatteredpixeldungeon) **/
    private final String ROOT;
    public PackageTrie(String ROOT) {this.ROOT = ROOT;}

    public PackageTrie() { this("com.shatteredpixel.shatteredpixeldungeon"); }  // backwards compatibility

    private final HashMap<String, PackageTrie> subTries = new HashMap<>();
    private final ArrayList<Class<?>> classes = new ArrayList<>();
    private HashMap<Class<?>, HashMap<String, Class<?>>> localizedClassCache = new HashMap<>();

    public final Map<String,PackageTrie> getSubtries()  { return unmodifiableMap(subTries); }
    public final List<Class<?>> getClasses()            { return unmodifiableList(classes); }

    protected void add(String pkg, PackageTrie tree) {
        if(!tree.isEmpty()) subTries.put(pkg, tree);
    }

    /** finds a package somewhere in the trie.
     *
     * fixme/todo This is not used for #findClass because it stops at first match. If it returned a list it would work, probably.
     * fixme this (and #findClass) do not handle duplicated results well at all. This isn't an issue for me, but it COULD be an issue.
     **/
    public PackageTrie findPackage(String name) {
        return findPackage(name.split("\\."), 0);
    }
    public PackageTrie findPackage(String[] path, int index) {
        if(index == path.length) return this;

        PackageTrie
                match = getPackage(path[index]),
                found = match != null ? match.findPackage(path, index+1) : null;

        if(found != null) return found;
        for(PackageTrie trie : subTries.values()) {
            if(trie == match) continue;
            found = trie.findPackage(path, 0);
            if(found != null) return found;
        }
        return null;
    }

    public Class<?> findClass(String name, Class<?> parent) {
        // first attempt to blindly match the class
        Class<?> match = null;
        try {
            match = Reflection.forNameUnhandled(name);
        } catch (ReflectionException e) {
            if(ROOT != null && !name.startsWith(ROOT)) {
                try {
                    match = Reflection.forNameUnhandled(ROOT + "." + name);
                }
                catch (ReflectionException ignored) {/* do nothing */}
                catch (Exception e1) {
                    e1.addSuppressed(e);
                    Game.reportException(e1);
                }
            }
        } catch(Exception e) {Game.reportException(e);}
        if (match != null && parent.isAssignableFrom(match)) {
            // add it to the trie if possible
            String pkg = match.getPackage().getName();
            addClass(match, pkg.substring(pkg.indexOf(ROOT + ".") + 1));
            return match;
        }
        // now match it from stored classes
        match = findClass(name.split("\\."), parent, 0);
        if (match == null) {
            match = findLocalizedClass(name, parent);
        }
        return match;
    }
    // known issues: duplicated classes may mask each other.
    public Class<?> findClass(String[] path, Class parent, int i) {
        if(i == path.length) return null;

        Class<?> found = null;
        PackageTrie match = null;
        if(i+1 < path.length) {
            match = getPackage(path[i]);
            if (match != null) {
                found = match.findClass(path, parent, i + 1);
                if (found != null && (parent == null || parent.isAssignableFrom(found)) ) return found;
            }
        } else if( ( found = getClass(path[i]) ) != null && (parent == null || parent.isAssignableFrom(found))) return found;
        else found = null;
        ArrayList<PackageTrie> toSearch = new ArrayList(subTries.values());
        toSearch.remove(match);
        for(PackageTrie tree : toSearch) if( (found = tree.findClass(path,parent,i)) != null ) break;
        return found;
    }

    // does not deep search
    public PackageTrie getPackage(String packageName) {
        return subTries.get(packageName);
    }
    // this is probably not efficient or even taking advantage of what I've done.
    public Class<?> getClass(String className) {
        boolean hasQualifiers = className.contains("$") || className.contains(".");
        for(Class<?> cls : classes) {
            boolean match = hasQualifiers
                    ? cls.getName().toLowerCase(Locale.ROOT).endsWith( className.toLowerCase(Locale.ROOT) )
                    : cls.getSimpleName().equalsIgnoreCase(className);
            if(match) return cls;
        }
        return null;
    }

    public ArrayList<Class> getAllClasses() {
        ArrayList<Class> classes = new ArrayList(this.classes);
        for(PackageTrie tree : subTries.values()) classes.addAll(tree.getAllClasses());
        return classes;
    }

    private Class<?> findLocalizedClass(String name, Class<?> parent) {
        if (name == null || parent == null || parent == Object.class || parent == Class.class) {
            return null;
        }
        HashMap<String, Class<?>> aliases = localizedClassCache.get(parent);
        if (aliases == null) {
            aliases = buildLocalizedClassAliases(parent);
            localizedClassCache.put(parent, aliases);
        }
        return aliases.get(normalizeAlias(name));
    }

    private HashMap<String, Class<?>> buildLocalizedClassAliases(Class<?> parent) {
        HashMap<String, Class<?>> aliases = new HashMap<>();
        for (Class<?> cls : getAllClasses()) {
            if (parent.isAssignableFrom(cls)) {
                addAlias(aliases, cls.getSimpleName(), cls);
                addAlias(aliases, cls.getName(), cls);
                addAlias(aliases, cls.getName().replace('$', '.'), cls);
                addAlias(aliases, localizedNameFromMessages(cls), cls);
                addAlias(aliases, localizedNameFromInstance(cls, parent), cls);
            }
        }
        return aliases;
    }

    private static void addAlias(HashMap<String, Class<?>> aliases, String alias, Class<?> cls) {
        String normalized = normalizeAlias(alias);
        if (normalized.length() > 0 && !aliases.containsKey(normalized)) {
            aliases.put(normalized, cls);
        }
    }

    private static String normalizeAlias(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("_", "")
                .replace("-", "")
                .replace(" ", "")
                .replace("\t", "")
                .replace("\n", "")
                .replace("　", "")
                .replace("·", "")
                .replace(".", "")
                .replace("$", "")
                .toLowerCase(Locale.ROOT);
    }

    private static String localizedNameFromMessages(Class<?> cls) {
        String value = Messages.get(cls, "name");
        String normalized = normalizeAlias(value);
        if (normalized.length() == 0 || value.endsWith(".name") && value.indexOf('.') >= 0) {
            return null;
        }
        return value;
    }

    private static String localizedNameFromInstance(Class<?> cls, Class<?> parent) {
        if (!canSafelyInstantiateForAlias(cls, parent)) {
            return null;
        }
        try {
            Object instance = Reflection.newInstance(cls);
            if (instance instanceof Item) {
                return ((Item) instance).name();
            } else if (instance instanceof Buff) {
                Method name = cls.getMethod("name");
                if (name.getReturnType() == String.class) {
                    return (String) name.invoke(instance);
                }
            }
        } catch (Exception ignored) {
            // Some debug targets need runtime state or constructor side effects; skip those.
        }
        return null;
    }

    private static boolean canSafelyInstantiateForAlias(Class<?> cls, Class<?> parent) {
        if (Modifier.isAbstract(cls.getModifiers()) || cls.isEnum() || cls.isInterface()) {
            return false;
        }
        if (Reflection.isMemberClass(cls) && !Modifier.isStatic(cls.getModifiers())) {
            return false;
        }
        return Item.class.isAssignableFrom(parent) || Buff.class.isAssignableFrom(parent);
    }

    public boolean isEmpty() { return subTries.isEmpty() && classes.isEmpty(); }

    protected final PackageTrie getOrCreate(String pkg) {
        if(pkg == null || pkg.isEmpty()) return this;
        String[] split = pkg.split("\\.", 2);
        // [0] is stored, [1] is recursively added.
        PackageTrie stored = subTries.get(split[0]);
        if(stored == null) subTries.put(split[0], stored = new PackageTrie());
        return split.length == 1 ? stored : stored.getOrCreate(split[1]);
    }
    protected final void addClass(Class cls, String pkg) {
        String clsPkg = cls.getPackage().getName();
        if(clsPkg.equals(pkg)) classes.add(cls);
        else if(clsPkg.startsWith(pkg)) getOrCreate(clsPkg.substring(pkg.length()+1)).classes.add(cls);
    }

    public final void addPlatformClass(Class cls, String pkg) {
        addClass(cls, pkg);
    }

    /**
     * Attempts to list all the classes in the specified package as determined
     * by the context class loader
     *
     * @link https://stackoverflow.com/a/22462785/4258976
     *
     * @implNote I modified it to work with a trie, but that implementation will still get all classes.
     *
     * @param pckgname
     *            the package name to search
     * @return a trie of classes found in that package.
     * @throws ClassNotFoundException
     *             if something went wrong
     */
    public static PackageTrie getClassesForPackage(String pckgname)
            throws ClassNotFoundException {
        PackageTrie platformTrie = getPlatformClassesForPackage(pckgname);
        if (platformTrie != null) {
            return platformTrie;
        }

        PackageTrie root = new PackageTrie();
        ClassLoader loader = PackageTrie.class.getClassLoader();

        try {
            if (loader == null) throw new ClassNotFoundException("Can't get class loader.");

            final Enumeration<URL> resources = loader.getResources(pckgname.replace('.', '/'));
            URLConnection connection;

            while(resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if(url == null) break;
                try {
                    connection = url.openConnection();

                    if (connection instanceof JarURLConnection) {
                        checkJarFile((JarURLConnection) connection, pckgname, root);
                    } else if (url.getProtocol().equals("file")) {
                        try {
                            checkDirectory(
                                    new File(URLDecoder.decode(url.getPath(),
                                            "UTF-8")), pckgname, root);
                        } catch (final UnsupportedEncodingException ex) {
                            throw new ClassNotFoundException(
                                    pckgname + " does not appear to be a valid package (Unsupported encoding)",
                                    ex);
                        }
                    } else
                        throw new ClassNotFoundException(
                                pckgname +" ("+ url.getPath() +") does not appear to be a valid package");
                } catch (final IOException ioex) {
                    throw new ClassNotFoundException(
                            "IOException was thrown when trying to get all resources for "
                                    + pckgname, ioex);
                }
            }
        } catch (final NullPointerException ex) {
            throw new ClassNotFoundException(
                    pckgname+" does not appear to be a valid package (Null pointer exception)",
                    ex);
        } catch (final IOException ioex) {
            throw new ClassNotFoundException(
                    "IOException was thrown when trying to get all resources for "
                            + pckgname, ioex);
        }
        return root;
    }

    private static PackageTrie getPlatformClassesForPackage(String pckgname) {
        if (Game.platform == null) {
            return null;
        }
        try {
            Method method = Game.platform.getClass().getMethod("findClasses", String.class);
            Object result = method.invoke(Game.platform, pckgname);
            return result instanceof PackageTrie ? (PackageTrie) result : null;
        } catch (NoSuchMethodException ignored) {
            return null;
        } catch (Exception e) {
            Game.reportException(e);
            return null;
        }
    }
    private static PackageTrie checkDirectory(File directory, String pckgname, PackageTrie trie) throws ClassNotFoundException {
        File tmpDirectory;

        if (directory.exists() && directory.isDirectory()) {
            final String[] files = directory.list();

            for (final String file : files) {
                if (file.endsWith(".class")) {
                    try {
                        Class cls = Class.forName(pckgname + '.'
                                + file.substring(0, file.length() - 6));
                        //if(canInstantiate(cls))
                        trie.classes.add(cls);
                    } catch (final NoClassDefFoundError e) {
                        // do nothing. this class hasn't been found by the
                        // loader, and we don't care.
                    }
                } else if ( (tmpDirectory = new File(directory, file) ).isDirectory()) {
                    trie.add(file, checkDirectory(tmpDirectory, pckgname + "." + file, new PackageTrie()));
                }
            }
        }
        return trie;
    }
    private static void checkJarFile(JarURLConnection connection,
                                     String pckgname,
                                     PackageTrie tree)
            throws ClassNotFoundException, IOException {
        final JarFile jarFile = connection.getJarFile();
        final Enumeration<JarEntry> entries = jarFile.entries();

        while(entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if(jarEntry == null) break;

            String name = jarEntry.getName();
            int index = name.indexOf(".class");
            if(index == -1) continue;

            name = name.substring(0, index)
                    .replace('/', '.');
            if (name.contains(pckgname) /*&& canInstantiate(cls = Class.forName(name))*/) {
                tree.addClass(Class.forName(name),pckgname);
            }
        }
    }
}
