package sdt.wizards.change;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;

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
			case 'F':
				file = SDTPlugin.getFile(args.get(1));
				text = SDTPlugin.getTpl(context, "tpl/" + tid + "/" + key + ".vm");
				change = SDTPlugin.createNewFileChange(file, text);
				f.add(change);
				break;
			case 'M':
				file = SDTPlugin.getFile(args.get(1));
				text = SDTPlugin.getTpl(context, "tpl/" + tid + "/" + key + ".vm");
				change = SDTPlugin.createReplaceEdit(file, text, args.subList(2, args.size())
						.toArray(new String[0]));
				f.add(change);
				break;
			case 'D':
				change = new CreateDirChange(args.get(1));
				f.add(change);
				break;
			case 'P':
				change = new CreatePackageChange(args.get(1), args.get(2));
				f.add(change);
				break;
			}

		}

		return f.toArray(new Change[0]);
	}

	private static List<String> getArgs(String line) {
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
