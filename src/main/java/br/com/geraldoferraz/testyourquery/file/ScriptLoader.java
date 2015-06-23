package br.com.geraldoferraz.testyourquery.file;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ScriptLoader {

	private String fileName;

	public ScriptLoader(String fileName) {
		this.fileName = fileName;
	}

	public List<String> load() throws Exception {
		
		StringBuilder result = new StringBuilder("");

		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(fileName).getFile());

		Scanner scanner = new Scanner(file);

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			result.append(line).append("\n");
		}

		scanner.close();
		
		String[] split = result.toString().split(";");
		
		List<String> retorno = new ArrayList<String>(asList(split));
		return retorno;
	}

}
