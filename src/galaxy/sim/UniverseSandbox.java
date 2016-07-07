package galaxy.sim;

import static org.lwjgl.opengl.GL11.*;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.*;
import org.lwjgl.*;

public class UniverseSandbox {

	public static double FPS = 60, FRAMEWIDTH = 1280, FRAMEHEIGHT = 720, FRAME = 0, FRAMESKIP = 600, RUNTIME = 6000000;
	public static boolean SCREENCAP = false, RENDERLIMIT = false;

	public static double PRECISION = 10, SPEEDSQUARED = Math.pow(10, 8), scale = 4 * Math.pow(10, -10);
	//public static double minimumGravityAttractionRange = Math.pow(10, UniverseSandbox.PRECISION);

	public static double cameraX = (FRAMEWIDTH / 2), cameraY = (FRAMEHEIGHT / 2);

	// Universal Constants and Measures
	public static double G = 6.673 * Math.pow(10, -11); // Newton meters Squared
														// per kg Squared
	public static double solarmass = 1.989 * Math.pow(10, 30); // kg
	public static double solarradius = 6.957 * Math.pow(10, 8); // meters
	public static double blackholemass = 8.2 * Math.pow(10, 36); // kg
	public static double blackholeradius = 2.25 * Math.pow(10, 10); // meters
	public static double radius_of_milky_way = 5 * Math.pow(10, 20); // meters
	public static double speed_of_light = 299792458; // meters per second

	boolean rulerStart = false;
	double rulerX = 0, rulerY = 0; // in meters

	static List<PointOfMass> stars = new ArrayList<PointOfMass>(1);

	public boolean paused = false;

	private static String screenshotFolder = "D:/Phys Sim/Solar System/4th/";

	public static void main(String[] args) {
		if (SCREENCAP) {
			File theDir = new File(screenshotFolder);

			// if the directory does not exist, create it
			if (!theDir.exists()) {

				try {
					theDir.mkdir();
				} catch (SecurityException se) {
					// handle it
				}
			}
		}
		new UniverseSandbox();
	}

	public void init() {
		// always here code OGL
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, FRAMEWIDTH, 0, FRAMEHEIGHT, 1, -1);
		glMatrixMode(GL_MODELVIEW);
	}

	public void spawnBalls() {
		spawnSolarSystem(0, 0);
		// spawnGalaxy(0, 0);
		// fun_render_1();
	}

	private void fun_render_1() {
		stars.add(new PointOfMass(((2 * FRAMEWIDTH / 10) - (FRAMEWIDTH / 2)) / scale, ((2 * FRAMEHEIGHT / 10) - (FRAMEHEIGHT / 2)) / scale, 0, 0, 8.2 * Math.pow(10, 36), 0, 0, 0, solarradius));
		spawnGalaxy(((8 * FRAMEWIDTH / 10) - (FRAMEWIDTH / 2)) / scale, ((8 * FRAMEHEIGHT / 10) - (FRAMEHEIGHT / 2)) / scale);
	}

	// mouse inputs
	public void mouseaction() {
		if (Mouse.isButtonDown(0)) { // add stars of random color at the cursor
										// with random velocity vector
			stars.add(new PointOfMass((Mouse.getX() - cameraX) / scale, (Mouse.getY() - cameraY) / scale,
					0 * Math.random(), 2 * Math.random() * Math.PI, solarmass, Math.random(), (Math.random() + 1) * .5,
					(Math.random() + 1) * .5, solarradius));
		}

		if (Mouse.isButtonDown(2)) { // pan screen with mouse
			double dx = Mouse.getDX();
			double dy = Mouse.getDY();

			cameraX += dx;
			cameraY += dy;

		}

		if (Mouse.isButtonDown(1)) {
			ruler((Mouse.getX() - cameraX) / scale, (Mouse.getY() - cameraY) / scale);
		} else if (rulerStart) {
			rulerStart = false;
		}

		while (Mouse.next()) {
			if (Mouse.isButtonDown(4) && Mouse.getEventButton() == 4) {
				stars.add(new PointOfMass((Mouse.getX() - cameraX) / scale, (Mouse.getY() - cameraY) / scale,
						0 * Math.random(), 2 * Math.random() * Math.PI, solarmass, Math.random(),
						(Math.random() + 1) * .5, (Math.random() + 1) * .5, solarradius));
			}
		}

		int mouseWheel = Mouse.getDWheel() / 120;
		if (mouseWheel != 0) {
			scale *= Math.pow(1.1, mouseWheel);
			cameraX = cameraX * Math.pow(1.1, mouseWheel);
			cameraY = cameraY * Math.pow(1.1, mouseWheel);

		}
	}

	// keyboard inputs
	public void keypresses() {
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			close();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_X)) {
			stars.add(new PointOfMass(FRAMEWIDTH / (scale * 2) - (cameraX / scale),
					FRAMEHEIGHT / (scale * 2) - (cameraY / scale), 0.1 * Math.random(), 2 * Math.random() * Math.PI, 1,
					Math.random(), Math.random(), Math.random(), solarradius));
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_V)) {
			double dx = Mouse.getDX();
			double dy = Mouse.getDY();

			cameraX += dx;
			cameraY += dy;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			cameraX -= 10;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			cameraX += 10;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			cameraY -= 10;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			cameraY += 10;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
			stars.add(new PointOfMass(((FRAMEWIDTH * Math.random()) / scale) - (cameraX / scale),
					((FRAMEHEIGHT * Math.random()) / scale) - (cameraY / scale), 100000000 * Math.random(),
					2 * Math.random() * Math.PI, solarmass, Math.random(), Math.random(), Math.random(), solarradius));

		}
		while (Keyboard.next()) {
			if (Keyboard.getEventKey() == Keyboard.KEY_C && Keyboard.getEventKeyState()) {
				stars.add(new PointOfMass((FRAMEWIDTH / (2 * scale)) - (cameraX / scale),
						(FRAMEHEIGHT / (2 * scale)) - (cameraY / scale), 5 * Math.random(), 2 * Math.random() * Math.PI,
						solarmass, Math.random(), Math.random(), Math.random(), solarradius));
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_S && Keyboard.getEventKeyState()) {
				System.out.println(stars.size());
			}

			if (Keyboard.getEventKey() == Keyboard.KEY_G && Keyboard.getEventKeyState()) {
				spawnGalaxy((Mouse.getX() - cameraX) / scale, (Mouse.getY() - cameraY) / scale);
			}

			if (Keyboard.getEventKey() == Keyboard.KEY_F && Keyboard.getEventKeyState()) {
				spawnCluster((Mouse.getX() - cameraX) / scale, (Mouse.getY() - cameraY) / scale);
			}

			if (Keyboard.getEventKey() == Keyboard.KEY_P && Keyboard.getEventKeyState()) {
				paused = !paused;
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_R && Keyboard.getEventKeyState()) {
				stars = new ArrayList<PointOfMass>(1);
				spawnBalls();
			}
		}
	}

	private void ruler(double x, double y) {
		if (!rulerStart) {
			rulerStart = true;
			rulerX = x;
			rulerY = y;
		} else {
			System.out.println(Math.sqrt(((rulerX - x) * (rulerX - x)) + ((rulerY - y) * (rulerY - y))) + " meters");
		}

	}

	private void spawnCluster(double d, double e) {
		double starmass = solarmass;
		double radius = Math.pow(10, 12);
		double number = 100;

		double galaxy = Math.random();

		for (int i = 0; i < number; i++) {
			double phi = 2 * Math.random() * Math.PI;

			double r = Math.random() * radius;

			double dx = (r * Math.cos(phi));
			double dy = (r * Math.sin(phi));

			if (Math.random() > galaxy) {
				stars.add(new PointOfMass(d + dx, e + dy, 5, phi + (Math.PI / 2), starmass, 1, (Math.random() + 1) * .5,
						(Math.random() + 1) * .3, solarradius));
			} else {
				stars.add(new PointOfMass(d + dx, e + dy, 5, phi + (Math.PI / 2), starmass, (Math.random() + 1) * .3,
						(Math.random() + 1) * .5, 1, solarradius));
			}
		}
	}

	private void spawnGalaxy(double x, double y) {
		double number_of_stars = 1000;

		double red = 0;
		double green = 0;
		double blue = 0;

		double galaxy_type = Math.random();

		stars.add(new PointOfMass(x, y, 0, 0, blackholemass, 0, 0, 0, blackholeradius));

		int count = 0;
		while (count < number_of_stars) {
			double phi = 2 * Math.random() * Math.PI;

			double r = Math.pow(10, 15) + Math.random() * radius_of_milky_way;

			double dx = (r * Math.cos(phi));
			double dy = (r * Math.sin(phi));

			double thisStarMass = solarmass * 100;

			double blackgrav = Math.sqrt((G * (blackholemass + thisStarMass) * SPEEDSQUARED) / (r));
			double extragrav = 0;
			;

			if (Math.random() > galaxy_type) {
				red = 1;
				green = (Math.random() + 1) * .5;
				blue = (Math.random() + 1) * .3;

			} else {
				red = (Math.random() + 1) * .3;
				green = (Math.random() + 1) * .5;
				blue = 1;
			}
			stars.add(new PointOfMass((x + dx), (y + dy), blackgrav + extragrav, phi + (Math.PI / 2), thisStarMass, red,
					green, blue, solarradius));
			count++;
		}
	}

	private void spawnSolarSystem(double x, double y) {

		double planet[][] = new double[8][7]; // planets 1 through 9; 1=mass,
												// 2=velocity, 3=orbit radius,
												// 4=planet radius

		planet[0][0] = 3.285 * Math.pow(10, 23); // mercury
		planet[0][1] = 47360;
		planet[0][2] = 5.791 * Math.pow(10, 10);
		planet[0][3] = 2.4397 * Math.pow(10, 6);
		planet[0][4] = 117;
		planet[0][5] = 115;
		planet[0][6] = 117;

		planet[1][0] = 4.867 * Math.pow(10, 24); // venus
		planet[1][1] = 35020;
		planet[1][2] = 1.0821 * Math.pow(10, 11);
		planet[1][3] = 6.0518 * Math.pow(10, 6);
		planet[1][4] = 123;
		planet[1][5] = 108;
		planet[1][6] = 86;

		planet[2][0] = 5.972 * Math.pow(10, 24); // earth
		planet[2][1] = 29784.73;
		planet[2][2] = 1.4959787 * Math.pow(10, 11);
		planet[2][3] = 6.371 * Math.pow(10, 6);
		planet[2][4] = 104;
		planet[2][5] = 111;
		planet[2][6] = 165;

		planet[3][0] = 6.39 * Math.pow(10, 23); // mars
		planet[3][1] = 24000;
		planet[3][2] = 2.28 * Math.pow(10, 11);
		planet[3][3] = 3.39 * Math.pow(10, 6);
		planet[3][4] = 160;
		planet[3][5] = 87;
		planet[3][6] = 21;

		planet[4][0] = 1.898 * Math.pow(10, 27); // jupiter
		planet[4][1] = 13100;
		planet[4][2] = 7.786 * Math.pow(10, 11);
		planet[4][3] = 6.9911 * Math.pow(10, 7);
		planet[4][4] = 213;
		planet[4][5] = 191;
		planet[4][6] = 161;

		planet[5][0] = 5.683 * Math.pow(10, 26); // saturn
		planet[5][1] = 9600;
		planet[5][2] = 1.433 * Math.pow(10, 12);
		planet[5][3] = 5.8232 * Math.pow(10, 7);
		planet[5][4] = 174;
		planet[5][5] = 155;
		planet[5][6] = 116;

		planet[6][0] = 8.681 * Math.pow(10, 25); // uranus
		planet[6][1] = 6800;
		planet[6][2] = 2.870658 * Math.pow(10, 12);
		planet[6][3] = 2.5362 * Math.pow(10, 7);
		planet[6][4] = 182;
		planet[6][5] = 219;
		planet[6][6] = 224;

		planet[7][0] = 1.024 * Math.pow(10, 26); // neptune
		planet[7][1] = 5400;
		planet[7][2] = 4.495 * Math.pow(10, 12);
		planet[7][3] = 2.4622 * Math.pow(10, 7);
		planet[7][4] = 62;
		planet[7][5] = 93;
		planet[7][6] = 226;

		stars.add(new PointOfMass(x, y, 0, 0, solarmass, 255, 255, 0, solarradius));

		for (int i = 0; i < 8; i++) {
			stars.add(new PointOfMass((x + planet[i][2]), 0, planet[i][1] * Math.sqrt(SPEEDSQUARED), Math.PI / 2,
					planet[i][0], planet[i][4] / 255, planet[i][5] / 255, planet[i][6] / 255, planet[i][3]));

		}
		
		int count = 0;
		while (count < 700) {
			double phi = 2 * Math.random() * Math.PI;

			double r = (3 * Math.pow(10, 11)) + (Math.random() * 4 * Math.pow(10, 11));

			double dx = (r * Math.cos(phi));
			double dy = (r * Math.sin(phi));

			double thisMass = 3 * Math.pow(10,  16);

			double gravA = Math.sqrt((G * (solarmass + thisMass) * SPEEDSQUARED) / (r));
			
			double red = .5;
			double green = .3;
			double blue = .2;
			
			stars.add(new PointOfMass((x + dx), (y + dy), gravA, phi + (Math.PI / 2), thisMass, red,
					green, blue, 50000));
			count++;
		}
		
		count = 0;
		while (count < 200) {
			double phi = 2 * Math.random() * Math.PI;

			double r = (30 * planet[2][2]) + (Math.random() * 20 * planet[2][2]);

			double dx = (r * Math.cos(phi));
			double dy = (r * Math.sin(phi));

			double thisMass = Math.pow(10,  22);

			double gravA = Math.sqrt((G * (solarmass + thisMass) * SPEEDSQUARED) / (r));
			
			double red = .4;
			double green = .3;
			double blue = .2;
			
			stars.add(new PointOfMass((x + dx), (y + dy), gravA, phi + (Math.PI / 2), thisMass, red,
					green, blue, 50000));
			count++;
		}
	}

	public void efficientComp() {
		for (int i = 0; i < stars.size(); i++) {
			PointOfMass a = stars.get(i);
			for (int j = i + 1; j < stars.size(); j++) {
				PointOfMass b = stars.get(j);
				a.attractedTo(b);
			}
		}
		for (PointOfMass b : stars) {
			b.update();
		}
	}

	public void render() {

		glClear(GL_COLOR_BUFFER_BIT);

		for (PointOfMass b : stars) {
			b.draw();
		}
	}

	// i got this screenshot code from stack overflow. It worked so well I had
	// no need to make my own.
	private void screenShot() {

		// Creating an rbg array of total pixels
		int[] pixels = new int[(int) (FRAMEWIDTH * FRAMEHEIGHT)];
		int bindex;
		// allocate space for RBG pixels
		ByteBuffer fb = ByteBuffer.allocateDirect((int) (FRAMEWIDTH * FRAMEHEIGHT * 3));

		// grab a copy of the current frame contents as RGB
		glReadPixels(0, 0, (int) FRAMEWIDTH, (int) FRAMEHEIGHT, GL_RGB, GL_UNSIGNED_BYTE, fb);

		BufferedImage imageIn = new BufferedImage((int) FRAMEWIDTH, (int) FRAMEHEIGHT, BufferedImage.TYPE_INT_RGB);
		// convert RGB data in ByteBuffer to integer array
		for (int i = 0; i < pixels.length; i++) {
			bindex = i * 3;
			pixels[i] = ((fb.get(bindex) << 16)) + ((fb.get(bindex + 1) << 8)) + ((fb.get(bindex + 2) << 0));
		}
		// Allocate colored pixel to buffered Image
		imageIn.setRGB(0, 0, (int) FRAMEWIDTH, (int) FRAMEHEIGHT, pixels, 0, (int) FRAMEWIDTH);

		// Creating the transformation direction (horizontal)
		AffineTransform at = AffineTransform.getScaleInstance(1, -1);
		at.translate(0, -imageIn.getHeight(null));

		// Applying transformation
		AffineTransformOp opRotated = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		BufferedImage imageOut = opRotated.filter(imageIn, null);

		try {// Try to create image, else show exception.
			ImageIO.write(imageOut, "png", new File(screenshotFolder + "capture" + (int) (FRAME / FRAMESKIP) + ".png"));
		} catch (Exception e) {
			System.out.println("ScreenShot() exception: " + e);
		}
	}

	public void close() {

		Display.destroy();
		System.exit(0);
	}

	public UniverseSandbox() {

		try {
			Display.setDisplayMode(new DisplayMode((int) FRAMEWIDTH, (int) FRAMEHEIGHT));
			Display.setTitle("Play with the universe!");
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		init();
		spawnBalls();

		while (!Display.isCloseRequested()) {

			mouseaction();
			keypresses();
			render();
			Display.update();
			if (!paused) {
				if (FRAME % FRAMESKIP == 0 && SCREENCAP) {
					screenShot();
				}

				FRAME++;

				if (FRAME > RUNTIME && RENDERLIMIT) {
					close();
				}

				efficientComp();
			}
			// loop display
			Display.sync((int) FPS);
		}

		close();
	}
}

class PointOfMass {

	double drawAngle = 15;

	private int SEGMENTS = (int) (360 / drawAngle);

	public double x, y, vx, vy, vxn, vyn, m;
	private double colorRed, colorBlue, colorGreen;
	private double radius;

	private double[][] ballPoints = new double[SEGMENTS][2];
	private double[][] ballPointsSmall = new double[SEGMENTS][2];

	PointOfMass(double x, double y, double v, double theta, double mass, double red, double green, double blue,
			double radius) {
		this.x = x;
		this.y = y;
		this.vx = v * Math.cos(theta);
		this.vy = v * Math.sin(theta);
		this.vxn = 0;
		this.vyn = 0;
		this.m = mass;
		this.radius = radius;

		colorRed = red;
		colorGreen = green;
		colorBlue = blue;

		tellTheCircleHowToBeDrawn();
		tellTheCircleHowToBeDrawn(1);
	}

	private void tellTheCircleHowToBeDrawn() {
		for (int i = 0; i < SEGMENTS; i++) {
			ballPoints[i][0] = radius * Math.cos(Math.toRadians(i * drawAngle));
			ballPoints[i][1] = radius * Math.sin(Math.toRadians(i * drawAngle));
		}
	}

	private void tellTheCircleHowToBeDrawn(double radius) {
		for (int i = 0; i < SEGMENTS; i++) {
			ballPointsSmall[i][0] = radius * Math.cos(Math.toRadians(i * drawAngle));
			ballPointsSmall[i][1] = radius * Math.sin(Math.toRadians(i * drawAngle));
		}
	}

	boolean inClick(int mousex, int mousey) {
		double dx2 = (this.x - mousex) * (this.x - mousex);
		double dy2 = (this.y - mousey) * (this.y - mousey);
		if (Math.sqrt(dx2 + dy2) < this.radius) {
			return true;
		}
		return false;
	}

	public void attractedTo(PointOfMass that) {
		if (that != this) {
			if (that.vxn == 0 && that.vyn == 0) {
				that.vxn = that.vx;
				that.vyn = that.vy;
			}

			if (vxn == 0 && vyn == 0) {
				vxn = vx;
				vyn = vy;
			}

			double dx2 = (this.x - that.x) * (this.x - that.x);
			double dy2 = (this.y - that.y) * (this.y - that.y);

			effGrav(that, dx2 + dy2);

		}
	}

	public void effGrav(PointOfMass that, double r2) {
		double G = UniverseSandbox.G;

		double h = Math.sqrt(r2);

		double dx = that.x - this.x;
		double dy = that.y - this.y;

		double fx = 0;
		double fy = 0;

		if ((h > this.radius) && (h > that.radius)) {
			fx = (G * m * that.m * (dx / h)) / (r2);
			fy = (G * m * that.m * (dy / h)) / (r2);

			that.vxn -= (fx * UniverseSandbox.SPEEDSQUARED) / (that.m);
			that.vyn -= (fy * UniverseSandbox.SPEEDSQUARED) / (that.m);

			vxn += (fx * UniverseSandbox.SPEEDSQUARED) / (m);
			vyn += (fy * UniverseSandbox.SPEEDSQUARED) / (m);
		} else {
			if (this.m >= that.m) {
				this.vxn = ((this.m * this.vx) + (that.m * that.vx)) / (this.m + that.m);
				this.vyn = ((this.m * this.vy) + (that.m * that.vy)) / (this.m + that.m);
				this.m += that.m;
				this.radius = Math.sqrt((this.radius * this.radius) + (that.radius * that.radius));
				UniverseSandbox.stars.remove(that);
				tellTheCircleHowToBeDrawn();
			} else {
				that.vxn = ((that.m * that.vx) + (this.m * this.vx)) / (that.m * 2);
				that.vyn = ((that.m * that.vy) + (this.m * this.vy)) / (that.m * 2);
				that.m += this.m;
				that.radius = Math.sqrt((this.radius * this.radius) + (that.radius * that.radius));
				UniverseSandbox.stars.remove(this);
				that.tellTheCircleHowToBeDrawn();
			}
		}

	}

	public void update() {
		if (vxn != 0 && vyn != 0) {
			vx = vxn;
			vy = vyn;
			vxn = vyn = 0;
		}

		x += vx;
		y += vy;

	}

	public void draw() {
		glColor3f((float) colorRed, (float) colorGreen, (float) colorBlue);
		// glColor3f(1, 1, 1);

		double xToDraw = (x * UniverseSandbox.scale) + UniverseSandbox.cameraX;
		double yToDraw = (y * UniverseSandbox.scale) + UniverseSandbox.cameraY;

		double radiusToDraw = radius * UniverseSandbox.scale;

		if (radiusToDraw < 1) {
			glBegin(GL_POLYGON);
			for (int i = 0; i < SEGMENTS; i++) {
				glVertex2i((int) (xToDraw + ballPointsSmall[i][0]), (int) (yToDraw + ballPointsSmall[i][1]));
			}
			glEnd();
		} else {
			glBegin(GL_POLYGON);
			for (int i = 0; i < SEGMENTS; i++) {
				glVertex2i((int) (xToDraw + ballPoints[i][0] * UniverseSandbox.scale),
						(int) (yToDraw + ballPoints[i][1] * UniverseSandbox.scale));
			}
			glEnd();

		}

	}
}
