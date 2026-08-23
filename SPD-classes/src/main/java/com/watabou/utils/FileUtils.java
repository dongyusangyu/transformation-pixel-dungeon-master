/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.watabou.utils;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.watabou.noosa.Game;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class FileUtils {
	
	// Helper methods for setting/using a default base path and file address mode
	
	private static Files.FileType defaultFileType = null;
	private static String defaultPath = "";
	
	public static void setDefaultFileProperties( Files.FileType type, String path ){
		defaultFileType = type;
		defaultPath = path;
	}
	
	public static FileHandle getFileHandle( String name ){
		return getFileHandle( defaultFileType, defaultPath, name );
	}
	
	public static FileHandle getFileHandle( Files.FileType type, String name ){
		return getFileHandle( type, "", name );
	}
	
	public static FileHandle getFileHandle( Files.FileType type, String basePath, String name ){
		switch (type){
			case Classpath:
				return Gdx.files.classpath( basePath + name );
			case Internal:
				return Gdx.files.internal( basePath + name );
			case External:
				return Gdx.files.external( basePath + name );
			case Absolute:
				return Gdx.files.absolute( basePath + name );
			case Local:
				return Gdx.files.local( basePath + name );
			default:
				return null;
		}
	}
	
	// Files

	//looks to see if there is any evidence of interrupted saving
	public static boolean cleanTempFiles(){
		return cleanTempFiles("");
	}

	public static synchronized boolean cleanTempFiles( String dirName ){
		FileHandle dir = getFileHandle(dirName);
		boolean recoveredFile = false;
		for (FileHandle file : dir.list()) {
			if (!file.isDirectory() && file.name().endsWith(".del")) {
				FileHandle original = siblingWithoutSuffix(file, ".del");
				if (deleteBundleArtifacts(original)) file.delete();
				recoveredFile = true;
			}
		}
		for (FileHandle file : dir.list()){
			if (file.isDirectory()){
				recoveredFile = cleanTempFiles(dirName + file.name() + "/") || recoveredFile;
			} else if (file.length() == 0) {
				file.delete();
			}
		}

		// Resolve interrupted writes before considering backups. Directory listing order is undefined.
		for (FileHandle file : dir.list()) {
			if (!file.isDirectory() && file.name().endsWith(".tmp")) {
				FileHandle original = siblingWithoutSuffix(file, ".tmp");
				if (siblingWithSuffix(original, ".del").exists()) continue;
				if (isValidBundle(file)
						&& (!isValidBundle(original) || file.lastModified() > original.lastModified())) {
					promoteTemp(file, original);
				} else {
					file.delete();
				}
				recoveredFile = true;
			}
		}

		for (FileHandle file : dir.list()) {
			if (!file.isDirectory() && file.name().endsWith(".bak")) {
				FileHandle original = siblingWithoutSuffix(file, ".bak");
				if (siblingWithSuffix(original, ".del").exists()) continue;
				if (!isValidBundle(original)) {
					if (isValidBundle(file)) {
						restoreBackup(file, original);
					} else {
						file.delete();
					}
					recoveredFile = true;
				}
			}
		}
		return recoveredFile;
	}

	private static FileHandle siblingWithoutSuffix(FileHandle file, String suffix) {
		String path = file.path();
		return getFileHandle(defaultFileType, "", path.substring(0, path.length() - suffix.length()));
	}

	private static FileHandle siblingWithSuffix(FileHandle file, String suffix) {
		return getFileHandle(defaultFileType, "", file.path() + suffix);
	}

	private static boolean isValidBundle(FileHandle file) {
		if (file == null || !file.exists() || file.isDirectory() || file.length() == 0) {
			return false;
		}
		try {
			bundleFromStream(file.read());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static boolean promoteTemp(FileHandle temp, FileHandle target) {
		FileHandle backup = siblingWithSuffix(target, ".bak");
		boolean preserveTarget = isValidBundle(target);
		try {
			if (preserveTarget) {
				if (backup.exists()) backup.delete();
				target.moveTo(backup);
			} else if (target.exists()) {
				target.delete();
			}
			temp.moveTo(target);
			if (!isValidBundle(target)) {
				throw new IOException("Recovered temporary bundle failed validation");
			}
			return true;
		} catch (Exception e) {
			if (target.exists() && !isValidBundle(target)) target.delete();
			if (isValidBundle(backup)) {
				try {
					backup.copyTo(target);
				} catch (Exception restoreFailure) {
					e.addSuppressed(restoreFailure);
				}
			}
			Game.reportException(e);
			return false;
		}
	}

	private static boolean restoreBackup(FileHandle backup, FileHandle target) {
		try {
			if (target.exists()) target.delete();
			backup.copyTo(target);
			return isValidBundle(target);
		} catch (Exception e) {
			Game.reportException(e);
			return false;
		}
	}
	
	public static boolean fileExists( String name ){
		FileHandle file = getFileHandle( name );
		return !getFileHandle(name + ".del").exists()
				&& file.exists() && !file.isDirectory() && file.length() > 0;
	}

	//returns length of a file in bytes, or 0 if file does not exist
	public static long fileLength( String name ){
		FileHandle file = getFileHandle( name );
		if (!file.exists() || file.isDirectory()){
			return 0;
		} else {
			return file.length();
		}
	}
	
	public static boolean deleteFile( String name ){
		return getFileHandle( name ).delete();
	}

	public static synchronized boolean deleteBundleFile(String name) {
		FileHandle file = getFileHandle(name);
		FileHandle marker = getFileHandle(name + ".del");
		boolean existed = file.exists()
				|| getFileHandle(name + ".tmp").exists()
				|| getFileHandle(name + ".bak").exists();
		try {
			marker.writeString("deleted", false);
		} catch (Exception e) {
			Game.reportException(e);
			return false;
		}
		boolean deleted = deleteBundleArtifacts(file);
		if (deleted) marker.delete();
		return existed && deleted;
	}

	private static boolean deleteBundleArtifacts(FileHandle file) {
		FileHandle temp = siblingWithSuffix(file, ".tmp");
		FileHandle backup = siblingWithSuffix(file, ".bak");
		if (file.exists()) file.delete();
		if (temp.exists()) temp.delete();
		if (backup.exists()) backup.delete();
		return !file.exists() && !temp.exists() && !backup.exists();
	}

	//replaces a file with junk data, for as many bytes as given
	//This is helpful as some cloud sync systems do not persist deleted, empty, or zeroed files
	public static void overwriteFile( String name, int bytes ){
		byte[] data = new byte[bytes];
		Arrays.fill(data, (byte)1);
		getFileHandle( name ).writeBytes(data, false);
	}
	
	// Directories
	
	public static boolean dirExists( String name ){
		FileHandle dir = getFileHandle( name );
		return dir.exists() && dir.isDirectory();
	}
	
	public static boolean deleteDir( String name ){
		FileHandle dir = getFileHandle( name );
		
		if (dir == null || !dir.isDirectory()){
			return false;
		} else {
			return dir.deleteDirectory();
		}
	}

	public static ArrayList<String> filesInDir( String name ){
		FileHandle dir = getFileHandle( name );
		ArrayList result = new ArrayList();
		if (dir != null && dir.isDirectory()){
			for (FileHandle file : dir.list()){
				result.add(file.name());
			}
		}
		return result;
	}
	
	// bundle reading
	
	//only works for base path
	public static synchronized Bundle bundleFromFile( String fileName ) throws IOException{
		if (getFileHandle(fileName + ".del").exists()) {
			throw new IOException("file was explicitly deleted");
		}
		try {
			FileHandle file = getFileHandle( fileName );
			if (!file.exists() || file.isDirectory() || file.length() == 0) {
				throw new IOException("file does not exist!");
			}
			return bundleFromStream(file.read());
		} catch (IOException | GdxRuntimeException failure){
			if (!fileName.endsWith(".bak")) {
				FileHandle backup = getFileHandle(fileName + ".bak");
				if (isValidBundle(backup)) {
					FileHandle file = getFileHandle(fileName);
					try {
						if (file.exists()) file.delete();
						backup.copyTo(file);
						return bundleFromStream(file.read());
					} catch (Exception ignored) {
						return bundleFromStream(backup.read());
					}
				}
			}
			//game classes expect an IO exception, so wrap GDX failures in one
			if (failure instanceof IOException) throw (IOException) failure;
			throw new IOException(failure);
		}
	}
	
	private static Bundle bundleFromStream( InputStream input ) throws IOException{
		try {
			return Bundle.read( input );
		} finally {
			input.close();
		}
	}
	
	// bundle writing
	
	//only works for base path
	public static synchronized void bundleToFile( String fileName, Bundle bundle ) throws IOException{
		FileHandle file = getFileHandle(fileName);
		FileHandle temp = getFileHandle(fileName + ".tmp");
		FileHandle backup = getFileHandle(fileName + ".bak");
		FileHandle deletionMarker = getFileHandle(fileName + ".del");
		try {
			if (temp.exists()) temp.delete();
			bundleToStream(temp.write(false), bundle);
			if (!isValidBundle(temp)) {
				temp.delete();
				throw new IOException("Temporary bundle validation failed: " + fileName);
			}
			if (deletionMarker.exists() && !deletionMarker.delete()) {
				temp.delete();
				throw new IOException("Could not clear deletion marker: " + fileName);
			}

			if (backup.exists()) backup.delete();
			if (file.exists()) file.moveTo(backup);

			try {
				temp.moveTo(file);
				if (!isValidBundle(file)) {
					throw new IOException("Committed bundle validation failed: " + fileName);
				}
			} catch (Exception e) {
				if (file.exists()) file.delete();
				if (backup.exists()) backup.moveTo(file);
				if (e instanceof IOException) throw (IOException) e;
				throw new IOException(e);
			}

		} catch (GdxRuntimeException e){
			//game classes expect an IO exception, so wrap the GDX exception in that
			throw new IOException(e);
		}
	}
	
	private static void bundleToStream( OutputStream output, Bundle bundle ) throws IOException{
		try {
			if (!Bundle.write(bundle, output)) {
				throw new IOException("Bundle serialization failed");
			}
		} finally {
			output.close();
		}
	}

}
