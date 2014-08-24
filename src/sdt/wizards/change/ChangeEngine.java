package sdt.wizards.change;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import sdt.SDTPlugin;

public class ChangeEngine {
	public static Change[] run(Map<String, Object> context, String tid) {
		Properties ps = new Properties();
		try {
			String dalConf = SDTPlugin.getTpl(context, "tpl/" + tid + "/conf.vm");
			ps.load(new ByteArrayInputStream(dalConf.getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Collection<Change> f = new ArrayList<Change>();
		for (Map.Entry<?, ?> entry : ps.entrySet()) {
			String key = entry.getKey().toString();
			List<String> args = getArgs(entry.getValue().toString());

			char changeAction = args.get(0).charAt(0);
			IFile file;
			String text;
			Change change;
			switch (changeAction) {
			case 'F': // new a text file
				file = SDTPlugin.getFile(args.get(1));
				text = SDTPlugin.getTpl(context, "tpl/" + tid + "/" + key + ".vm");
				change = createNewFileChange(file, text);
				f.add(change);
				break;
			case 'M': // modify a text file
				file = SDTPlugin.getFile(args.get(1));
				text = SDTPlugin.getTpl(context, "tpl/" + tid + "/" + key + ".vm");
				change = createReplaceEdit(file, text, args.subList(2, args.size()).toArray(new String[0]));
				f.add(change);
				break;
			case 'D': // new a dir
				change = new CreateDirChange(args.get(1));
				f.add(change);
				break;
			case 'P': // new a package
				change = new CreatePackageChange(args.get(1), args.get(2));
				f.add(change);
				break;
			}

		}

		return f.toArray(new Change[0]);
	}

	// TODO

	public static TextFileChange createNewFileChange(IFile targetFile, String contents) {
		String fileName = targetFile.getName();
		String message;
		if (targetFile.exists()) {
			message = String.format("Replace %1$s", new Object[] { fileName });
		} else {
			message = String.format("Create %1$s", new Object[] { fileName });
		}
		TextFileChange change = new TextFileChange(message, targetFile) {
			protected IDocument acquireDocument(IProgressMonitor pm) throws CoreException {
				IDocument document = super.acquireDocument(pm);
				if (document.getLength() > 0) {
					try {
						document.replace(0, document.getLength(), "");
					} catch (BadLocationException localBadLocationException) {
						localBadLocationException.printStackTrace();
					}
				}
				return document;
			}
		};

		change.setTextType(targetFile.getFileExtension());

		MultiTextEdit rootEdit = new MultiTextEdit();
		rootEdit.addChild(new InsertEdit(0, contents));
		change.setEdit(rootEdit);

		return change;
	}

	public static TextFileChange createReplaceEdit(IFile to, String text, String... param) {
		String oldText = SDTPlugin.readFile(to);
		String newText = sed(text, oldText, param);

		// TODO
		TextFileChange change = new TextFileChange("Merge " + to.getName(), to);
		MultiTextEdit rootEdit = new MultiTextEdit();
		rootEdit.addChild(new ReplaceEdit(0, oldText.length(), newText));
		change.setEdit(rootEdit);
		change.setTextType(to.getFileExtension());
		return change;
	}

	public static String sed(String insert, String toText, String... param) {

		if (param.length == 0 || insert == null || toText == null)
			return toText;
		String action = param[0], regex = param[1];
		char c = action.charAt(0);

		StringBuffer f = new StringBuffer(toText);
		switch (c) {
		case 'i':
			if (toText.contains(regex)) {
				int p = f.indexOf(regex);
				f.insert(p, insert);
			}
			break;
		case 'a':
			if (toText.contains(regex)) {
				int p = f.indexOf(regex);
				f.insert(p + regex.length(), insert);
			}
			break;
		case 'I': {
			String start = param[2], end = param[3];
			if (start != null && end != null && toText.contains(start)) {
				int startPosition = f.indexOf(start);
				int endPosition = f.indexOf(end, startPosition);
				int regexPosition = f.indexOf(regex, startPosition);
				if (regexPosition < endPosition) {
					f.insert(startPosition, insert);
				}
			}
		}
			break;
		case 'A': {
			String start = param[2], end = param[3];
			if (start != null && end != null && toText.contains(start)) {
				int startPosition = f.indexOf(start);
				int endPosition = f.indexOf(end, startPosition);
				int regexPosition = f.indexOf(regex, startPosition);
				if (regexPosition < endPosition) {
					f.insert(endPosition + end.length(), insert);
				}
			}
		}
			break;
		}

		return f.toString();
	}

	public static List<String> getArgs(String line) {
		List<String> c = new ArrayList<String>();
		for (String s : line.split(" ")) {
			String w = s.trim();
			if (w.isEmpty())
				continue;
			c.add(w);
		}
		return c;
	}

}
