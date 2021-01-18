package miniplc0java;

import miniplc0java.analyser.Analyser;
import miniplc0java.function.Function;
import miniplc0java.global.Global;
import miniplc0java.output.Output;
import miniplc0java.tokenizer.StringIterator;
import miniplc0java.tokenizer.Tokenizer;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.*;
import java.util.List;
import java.util.Scanner;

import static java.lang.System.exit;

public class App {
    public static void main(String[] args) {
        try{
            ArgumentParser argparse = buildArgparse();
            Namespace result;
            try {
                result = argparse.parseArgs(args);
            } catch (ArgumentParserException e1) {
                argparse.handleError(e1);
                return;
            }
            String inputFileName = result.getString("input");
            String outputFileName = result.getString("output");

            InputStream input;
            if (inputFileName.equals("-")) {
                input = System.in;
            } else {
                try {
                    input = new FileInputStream(inputFileName);
                } catch (FileNotFoundException e) {
                    System.err.println("Cannot find input file.");
                    e.printStackTrace();
                    System.exit(0);
                    return;
                }
            }

            PrintStream output;
            if (outputFileName.equals("-")) {
                output = System.out;
            } else {
                try {
                    output = new PrintStream(new FileOutputStream(outputFileName));
                } catch (FileNotFoundException e) {
                    System.err.println("Cannot open output file.");
                    e.printStackTrace();
                    System.exit(0);
                    return;
                }
            }

            //读入并开始
            //InputStream input = new FileInputStream("input.txt");
            Scanner scanner = new Scanner(input);
            StringIterator it = new StringIterator(scanner);
            Tokenizer tokenizer = new Tokenizer(it);
            Analyser analyser = new Analyser(tokenizer);
            analyser.startAnalyse();

            //debug测试
            System.out.println("全局符号表大小："+analyser.getGlobalTable().size());
            System.out.println("全局符号表：");
            for (Global global : analyser.getGlobalTable()) {
                System.out.println(global);
            }
            System.out.println("起始函数：\n"+analyser.get_start());
            System.out.println("函数：");
            for (Function function : analyser.getFunctionTable()) {
                System.out.println(function);
            }

            //输出格式转换
            Output out = new Output(analyser.getGlobalTable(), analyser.getFunctionTable(), analyser.get_start());
            List<Byte> bytes = out.transfer();
            byte[] resultX = new byte[bytes.size()];
            for (int i = 0; i < bytes.size(); ++i) {
                resultX[i] = bytes.get(i);
            }

            //输出
            //DataOutputStream output = new DataOutputStream(new FileOutputStream(new File("output.txt")));
            output.write(resultX);
        }
        catch (Exception e){
            exit(0);
        }
    }

    private static ArgumentParser buildArgparse() {
        var builder = ArgumentParsers.newFor("miniplc0-java");
        var parser = builder.build();
        parser.addArgument("-t", "--tokenize").help("Tokenize the input").action(Arguments.storeTrue());
        parser.addArgument("-l", "--analyse").help("Analyze the input").action(Arguments.storeTrue());
        parser.addArgument("-o", "--output").help("Set the output file").required(true).dest("output")
                .action(Arguments.store());
        parser.addArgument("file").required(true).dest("input").action(Arguments.store()).help("Input file");
        return parser;
    }
}
