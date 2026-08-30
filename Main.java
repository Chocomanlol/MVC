import Controller.ElectionController;
import Model.CSVDataLoader;
import Model.ElectionModel;
import View.ConsoleView;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        // 1. สร้าง Model
        ElectionModel model = new ElectionModel();

        // 2. โหลดข้อมูล seed data จากไฟล์ CSV เข้า Model
        // (กำหนด path ไปยังโฟลเดอร์ที่เก็บไฟล์ CSV)
        String csvFolderPath = "."; 
        CSVDataLoader.loadSeedData(model, csvFolderPath);

        // 3. สร้าง Controller โดยส่ง Model เข้าไป
        ElectionController controller = new ElectionController(model);

        // 4. สร้าง View โดยส่ง Controller เข้าไป
        ConsoleView view = new ConsoleView(controller);

        // 5. เริ่มต้นทำงาน UI
        view.start();
    }
}
