package app;

import java.awt.EventQueue;
import java.util.Scanner;

import computer.Computer;
import computer.ComputerAbstract;
import gui.MainFrame;
import utils.Format;
import utils.Logger;
import utils.Utils;

public class Main {

	private static final String CLI_HELP = Info.NAME + " Help\n" + "Enter:\t\tnext clock\n" + "h,?:\t\thelp\n"
			+ "q:\t\tquit\n" + "savelog:\tsave log file\n";

	private static final String CMD_HELP = "Usage: java -jar bce.jar [options] [file]\n" + "Options:\n"
			+ "  -d: file is decimal text file\n" + "  -x: file is hexadecimal text file\n"
			+ "  -b: file is binary text file\n" + "  -nogui: use command line interface\n"
			+ "  -m: quit after finishing execution and only print that memory location\n"
			+ "  -tick: Press 'Enter' to move to next clock cycle (only with -nogui)\n"
			+ "  -q: quit after finishing execution\n" + "  -v: output version information and exit\n"
			+ "  -h,?: display this help and exit\n";

	ComputerAbstract c;
	Logger logger;
	short[] M;
	short AR, PC, DR, AC, IR, TR;
	byte SC;
	boolean S, E;
	int lines = 10;
	int showMem = -1;

	public Main(String[] args) {
		String filePath = null;
		char fileType = 'a';
		boolean gui = true;
		boolean tick = false;
		boolean q = false;

		String o;
		for (int i = 0; i < args.length; ++i) {
			o = args[i];
			if (o.charAt(0) == '-') {
				o = o.substring(1);
				switch (o) {
				case "d":
				case "x":
				case "b":
					if (fileType != 'a')
						wrongInput();
					fileType = o.charAt(0);
					break;
				case "nogui":
					gui = false;
					break;
				case "m":
					try {
						showMem = Integer.parseInt(args[++i]);
						gui = false;
						q = true;
						tick = false;
						if (showMem < 0 || showMem >= ComputerAbstract.MEM_SIZE) {
							System.err.println(
									"Memory location should be between 0 and " + (ComputerAbstract.MEM_SIZE - 1));
							System.exit(1);
						}
					} catch (Exception e) {
						wrongInput();
					}
					break;
				case "tick":
					tick = true;
					break;
				case "q":
					q = true;
					break;
				case "v":
					System.out.println(Info.NAME + " version " + Info.VERSION);
					System.exit(0);
					break;
				case "h":
				case "?":
					System.out.println(CMD_HELP);
					System.exit(0);
					break;
				default:
					wrongInput();
					break;
				}
			} else {
				filePath = o;
			}
		}

		logger = new Logger();
		c = new Computer(logger);
		c.connectOnUpdate((_S, _M, _AR, _PC, _DR, _AC, _IR, _TR, _SC, _E) -> {
			S = _S;
			M = _M;
			AR = _AR;
			PC = _PC;
			DR = _DR;
			AC = _AC;
			IR = _IR;
			TR = _TR;
			SC = _SC;
			E = _E;
		});
		if (filePath != null)
			switch (fileType) {
			case 'a':
				c.loadProgramFromFile(filePath);
				break;
			case 'd':
				c.loadDecProgramFromFile(filePath);
				break;
			case 'x':
				c.loadHexProgramFromFile(filePath);
				break;
			case 'b':
				c.loadBinProgramFromFile(filePath);
				break;
			}
		if (gui) {
			startGui();
		} else {
			if (!tick)
				c.start();
			if (showMem != -1)
				System.out.println(M[showMem]);
			else
				runEveryClock(!q);
		}
	}

	private void wrongInput() {
		System.out.println(CMD_HELP);
		System.exit(1);
	}

	private void runEveryClock(boolean keepRunning) {
		c.tick();
		int mStart = 0;
		displayState(mStart);
		Scanner scanner = new Scanner(System.in);
		String input;
		loop: while (c.isRunning() || keepRunning) {
			switch (input = scanner.nextLine()) {
			case "":
				c.tick();
				displayState(mStart);
				break;
			case "h":
			case "?":
				System.out.println(CLI_HELP);
				break;
			case "savelog":
				System.out.print("save path: ");
				String path = scanner.nextLine();
				try {
					logger.save(path);
					System.out.println("Saved");
				} catch (Exception e) {
					System.err.println("Error saving log file");
				}
				break;
			case "q":
				break loop;
			default:
				try {
					mStart = Integer.parseInt(input);
					displayState(mStart);
				} catch (NumberFormatException e) {
					mStart = 0;
					System.out.println(CLI_HELP);
				}
			}
		}
		scanner.close();
	}

	private void displayState(int mStart) {
		try {
			System.out.print(
					Format.all(logger, lines = Utils.getTerminalLines(), mStart, S, M, AR, PC, DR, AC, IR, TR, SC, E));
		} catch (IndexOutOfBoundsException ie) {
			int max = Computer.MEM_SIZE - lines + 4;
			mStart = mStart > max ? max : 0;
			displayState(mStart);
		}
	}

	public void startGui() {
		EventQueue.invokeLater(() -> {
			try {
				new MainFrame(c).setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public static void main(String[] args) {
		new Main(args);
	}

}
