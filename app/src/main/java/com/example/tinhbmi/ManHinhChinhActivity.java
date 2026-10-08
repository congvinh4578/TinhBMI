package com.example.tinhbmi;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import java.util.List;

public class ManHinhChinhActivity extends AppCompatActivity {

    // Phạm vi của hai thanh trượt
    private static final int CHIEU_CAO_NHO_NHAT = 100;
    private static final int CAN_NANG_NHO_NHAT = 30;

    // Tuổi hợp lệ và hệ số tính cân nặng lý tưởng: 22 x (chiều cao m)^2
    private static final int TUOI_NHO_NHAT = 10;
    private static final int TUOI_LON_NHAT = 100;
    private static final float HE_SO_LY_TUONG = 22f;

    // Mã của 2 mục trong menu
    private static final int MUC_LICH_SU = 1;
    private static final int MUC_DANH_GIA = 2;

    // Các view trên màn hình
    private View boCucGoc;
    private View khuVucTieuDe;
    private ImageButton nutDoiGiaoDien;
    private ImageButton nutMenuBaSoc;
    private ScrollView cuonNoiDung;
    private SeekBar thanhTruotChieuCao;
    private SeekBar thanhTruotCanNang;
    private TextView nhanGiaTriChieuCao;
    private TextView nhanGiaTriCanNang;
    private RadioGroup nhomGioiTinh;
    private EditText nhapTuoi;
    private Button nutTinhBmi;
    private ProgressBar vongXoayTai;
    private CardView theKetQua;
    private TextView nhanChiSoBmi;
    private TextView nhanPhanLoai;
    private TextView nhanCanNangLyTuong;
    private Button nutXemChiTietLoiKhuyen;

    // Nhớ lần tính gần nhất để hiện lại kết quả khi màn hình bị tạo lại (ví dụ: đổi giao diện sáng/tối)
    private int chieuCaoDaTinh = 0;
    private int canNangDaTinh = 0;
    private int tuoiDaTinh = 22;
    private boolean laNamDaTinh = true;

    // Lời khuyên đầy đủ, chỉ hiện khi bấm nút "Xem lời khuyên chi tiết"
    private String loiKhuyenChiTiet = "";

    // Dùng để chờ 2,5 giây trước khi hiện kết quả
    private final Handler boXuLyTre = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Áp dụng giao diện sáng/tối đã lưu trước khi vẽ màn hình
        KhoDuLieu.ungDungCheDoGiaoDien(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_man_hinh_chinh);

        anhXaView();
        TienIch.thietLapToanManHinh(this, boCucGoc, khuVucTieuDe, null);
        thietLapThanhTruot();

        nutTinhBmi.setOnClickListener(v -> tinhToanBmi());
        nutXemChiTietLoiKhuyen.setOnClickListener(v -> hienHopThoaiLoiKhuyen());
        nutDoiGiaoDien.setOnClickListener(v -> doiGiaoDien());
        nutMenuBaSoc.setOnClickListener(v -> hienMenu(v));
        capNhatBieuTuongGiaoDien();

        if (savedInstanceState == null) {
            // Mở app lần đầu: đặt thanh trượt theo lần đo gần nhất
            List<BanGhiLichSu> lichSu = KhoDuLieu.layLichSu(this);
            if (!lichSu.isEmpty()) {
                thanhTruotChieuCao.setProgress(lichSu.get(0).chieuCao - CHIEU_CAO_NHO_NHAT);
                thanhTruotCanNang.setProgress(lichSu.get(0).canNang - CAN_NANG_NHO_NHAT);
            }
        } else {
            // Màn hình vừa được tạo lại: hiện lại kết quả đã tính
            chieuCaoDaTinh = savedInstanceState.getInt("chieu_cao_da_tinh", 0);
            canNangDaTinh = savedInstanceState.getInt("can_nang_da_tinh", 0);
            tuoiDaTinh = savedInstanceState.getInt("tuoi_da_tinh", 22);
            laNamDaTinh = savedInstanceState.getBoolean("la_nam_da_tinh", true);
            if (chieuCaoDaTinh > 0) {
                hienThiKetQua(chieuCaoDaTinh, canNangDaTinh, laNamDaTinh, tuoiDaTinh);
            }
        }
        capNhatNhanGiaTri();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        boXuLyTre.removeCallbacksAndMessages(null); // Hủy phép chờ đang dở
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle trangThaiLuu) {
        super.onSaveInstanceState(trangThaiLuu);
        trangThaiLuu.putInt("chieu_cao_da_tinh", chieuCaoDaTinh);
        trangThaiLuu.putInt("can_nang_da_tinh", canNangDaTinh);
        trangThaiLuu.putInt("tuoi_da_tinh", tuoiDaTinh);
        trangThaiLuu.putBoolean("la_nam_da_tinh", laNamDaTinh);
    }

    private void anhXaView() {
        boCucGoc = findViewById(R.id.bo_cuc_goc);
        khuVucTieuDe = findViewById(R.id.khu_vuc_tieu_de);
        nutDoiGiaoDien = findViewById(R.id.nut_doi_giao_dien);
        nutMenuBaSoc = findViewById(R.id.nut_menu_ba_soc);
        cuonNoiDung = findViewById(R.id.cuon_noi_dung);
        thanhTruotChieuCao = findViewById(R.id.thanh_truot_chieu_cao);
        thanhTruotCanNang = findViewById(R.id.thanh_truot_can_nang);
        nhanGiaTriChieuCao = findViewById(R.id.nhan_gia_tri_chieu_cao);
        nhanGiaTriCanNang = findViewById(R.id.nhan_gia_tri_can_nang);
        nhomGioiTinh = findViewById(R.id.nhom_gioi_tinh);
        nhapTuoi = findViewById(R.id.nhap_tuoi);
        nutTinhBmi = findViewById(R.id.nut_tinh_bmi);
        vongXoayTai = findViewById(R.id.vong_xoay_tai);
        theKetQua = findViewById(R.id.the_ket_qua);
        nhanChiSoBmi = findViewById(R.id.nhan_chi_so_bmi);
        nhanPhanLoai = findViewById(R.id.nhan_phan_loai);
        nhanCanNangLyTuong = findViewById(R.id.nhan_can_nang_ly_tuong);
        nutXemChiTietLoiKhuyen = findViewById(R.id.nut_xem_chi_tiet_loi_khuyen);
    }

    // ------------------------------------------------------------
    // Thanh trượt
    // ------------------------------------------------------------
    private void thietLapThanhTruot() {
        SeekBar.OnSeekBarChangeListener boLangNghe = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar thanhTruot, int tienTrinh, boolean tuNguoiDung) {
                capNhatNhanGiaTri();
            }

            @Override
            public void onStartTrackingTouch(SeekBar thanhTruot) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar thanhTruot) {
            }
        };
        thanhTruotChieuCao.setOnSeekBarChangeListener(boLangNghe);
        thanhTruotCanNang.setOnSeekBarChangeListener(boLangNghe);
    }

    private int layChieuCao() {
        return thanhTruotChieuCao.getProgress() + CHIEU_CAO_NHO_NHAT;
    }

    private int layCanNang() {
        return thanhTruotCanNang.getProgress() + CAN_NANG_NHO_NHAT;
    }

    private void capNhatNhanGiaTri() {
        nhanGiaTriChieuCao.setText(layChieuCao() + " cm");
        nhanGiaTriCanNang.setText(layCanNang() + " kg");
    }

    // ------------------------------------------------------------
    // Tính BMI
    // ------------------------------------------------------------
    private void tinhToanBmi() {
        // Kiểm tra tuổi trước, nhập sai thì dừng lại và nhắc người dùng
        String chuoiTuoi = nhapTuoi.getText().toString().trim();
        int tuoi = chuoiTuoi.isEmpty() ? 0 : Integer.parseInt(chuoiTuoi);
        if (tuoi < TUOI_NHO_NHAT || tuoi > TUOI_LON_NHAT) {
            Toast.makeText(this, "Vui lòng nhập tuổi từ " + TUOI_NHO_NHAT + " đến " + TUOI_LON_NHAT,
                    Toast.LENGTH_SHORT).show();
            nhapTuoi.requestFocus();
            return;
        }

        // Lấy số liệu ngay lúc bấm nút
        int chieuCao = layChieuCao();
        int canNang = layCanNang();
        boolean laNam = nhomGioiTinh.getCheckedRadioButtonId() == R.id.nut_nam;

        // Ẩn bàn phím nếu đang mở
        nhapTuoi.clearFocus();
        InputMethodManager quanLyBanPhim = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        quanLyBanPhim.hideSoftInputFromWindow(nhapTuoi.getWindowToken(), 0);

        // Bước 1: ẩn kết quả cũ, hiện vòng xoay, khóa nút để không bấm liên tục
        theKetQua.setVisibility(View.GONE);
        chieuCaoDaTinh = 0;
        vongXoayTai.setVisibility(View.VISIBLE);
        nutTinhBmi.setEnabled(false);
        nutTinhBmi.setAlpha(0.6f);

        // Bước 2: chờ 2,5 giây rồi mới tính và hiện kết quả
        boXuLyTre.postDelayed(() -> {
            vongXoayTai.setVisibility(View.GONE);

            float chiSoBmi = TienIch.tinhChiSoBmi(canNang, chieuCao);
            hienThiKetQua(chieuCao, canNang, laNam, tuoi);

            // Lưu lần đo này vào lịch sử
            BanGhiLichSu banGhiMoi = new BanGhiLichSu(System.currentTimeMillis(),
                    chieuCao, canNang, chiSoBmi, TienIch.layPhanLoai(chiSoBmi));
            KhoDuLieu.themLichSu(this, banGhiMoi);

            // Mở lại nút và cuộn xuống để thấy thẻ kết quả
            nutTinhBmi.setEnabled(true);
            nutTinhBmi.setAlpha(1f);
            cuonNoiDung.post(() -> cuonNoiDung.smoothScrollTo(0, theKetQua.getTop()));
            Toast.makeText(this, "Đã tính xong và lưu vào lịch sử!", Toast.LENGTH_SHORT).show();
        }, 2500);
    }

    private void hienThiKetQua(int chieuCao, int canNang, boolean laNam, int tuoi) {
        chieuCaoDaTinh = chieuCao;
        canNangDaTinh = canNang;
        laNamDaTinh = laNam;
        tuoiDaTinh = tuoi;

        float chiSoBmi = TienIch.tinhChiSoBmi(canNang, chieuCao);
        float chieuCaoMet = chieuCao / 100f;
        float canNangLyTuong = HE_SO_LY_TUONG * chieuCaoMet * chieuCaoMet;
        float canNangKhoeThap = TienIch.BMI_BINH_THUONG_THAP * chieuCaoMet * chieuCaoMet;
        float canNangKhoeCao = TienIch.BMI_BINH_THUONG_CAO * chieuCaoMet * chieuCaoMet;
        int mauSac = TienIch.layMauSac(this, chiSoBmi);

        // Chữ số BMI và nhãn phân loại đổi màu theo kết quả
        nhanChiSoBmi.setText(String.format(TienIch.NGON_NGU_VIET, "%.1f", chiSoBmi));
        nhanChiSoBmi.setTextColor(mauSac);

        nhanPhanLoai.setText(TienIch.layPhanLoai(chiSoBmi));
        GradientDrawable nenNhan = (GradientDrawable) nhanPhanLoai.getBackground().mutate();
        nenNhan.setColor(mauSac);
        nhanPhanLoai.setTextColor(TienIch.layMauChuTrenNhan(this, chiSoBmi));

        // Thẻ kết quả chỉ hiện 1 dòng ngắn, lời khuyên đầy đủ để dành cho hộp thoại
        loiKhuyenChiTiet = taoLoiKhuyen(chiSoBmi, canNang, canNangLyTuong, laNam, tuoi);
        nhanCanNangLyTuong.setText(String.format(TienIch.NGON_NGU_VIET,
                "Cân nặng lý tưởng: %.1f – %.1f kg", canNangKhoeThap, canNangKhoeCao));

        // Hiện thẻ kết quả với hiệu ứng mờ dần
        theKetQua.setVisibility(View.VISIBLE);
        theKetQua.setAlpha(0f);
        theKetQua.animate().alpha(1f).setDuration(350).start();
    }

    // Hộp thoại "Lời khuyên sức khỏe" (nội dung dài sẽ tự cuộn được)
    private void hienHopThoaiLoiKhuyen() {
        new AlertDialog.Builder(this)
                .setTitle("Lời khuyên sức khỏe")
                .setMessage(loiKhuyenChiTiet)
                .setPositiveButton("Đóng", null)
                .show();
    }

    // ------------------------------------------------------------
    // Bộ tạo lời khuyên thông minh (chỉ dùng if-else, không dùng AI)
    // Dựa trên: BMI + chênh lệch cân nặng + giới tính + nhóm tuổi
    // ------------------------------------------------------------
    private String taoLoiKhuyen(float chiSoBmi, int canNang, float canNangLyTuong, boolean laNam, int tuoi) {
        StringBuilder loiKhuyen = new StringBuilder();

        // Tình trạng cơ thể: 0 = gầy, 1 = bình thường, 2 = thừa cân hoặc béo phì
        int tinhTrang;
        if (chiSoBmi < TienIch.BMI_BINH_THUONG_THAP) {
            tinhTrang = 0;
        } else if (chiSoBmi < TienIch.BMI_RANH_GIOI_THUA_CAN) {
            tinhTrang = 1;
        } else {
            tinhTrang = 2;
        }

        // Nhóm tuổi: dưới 18 = thiếu niên, từ 40 = lớn tuổi, còn lại = trẻ
        boolean laThieuNien = tuoi < 18;
        boolean laLonTuoi = tuoi >= 40;

        loiKhuyen.append(String.format(TienIch.NGON_NGU_VIET,
                "Gợi ý dành cho bạn %s, %d tuổi.\n\n", laNam ? "nam" : "nữ", tuoi));

        // ----- 1. So sánh với cân nặng lý tưởng -----
        float chenhLech = canNang - canNangLyTuong; // số dương = đang nặng hơn mức lý tưởng
        float soKg = Math.abs(chenhLech);

        loiKhuyen.append("SO VỚI CÂN NẶNG LÝ TƯỞNG\n");
        if (soKg < 1f) {
            loiKhuyen.append(String.format(TienIch.NGON_NGU_VIET,
                    "• Cân nặng của bạn gần đúng mức lý tưởng (%.1f kg). Hãy giữ vững nhé!\n", canNangLyTuong));
        } else if (tinhTrang == 0) {
            loiKhuyen.append(String.format(TienIch.NGON_NGU_VIET,
                    "• Bạn nên tăng khoảng %.1f kg để đạt chuẩn (%.1f kg).\n", soKg, canNangLyTuong));
        } else if (tinhTrang == 2) {
            loiKhuyen.append(String.format(TienIch.NGON_NGU_VIET,
                    "• Bạn nên giảm khoảng %.1f kg để đạt chuẩn (%.1f kg).\n", soKg, canNangLyTuong));
        } else if (chenhLech > 0) {
            loiKhuyen.append(String.format(TienIch.NGON_NGU_VIET,
                    "• BMI của bạn vẫn khỏe mạnh, chỉ nặng hơn mức lý tưởng (%.1f kg) khoảng %.1f kg nên không cần giảm gấp.\n",
                    canNangLyTuong, soKg));
        } else {
            loiKhuyen.append(String.format(TienIch.NGON_NGU_VIET,
                    "• BMI của bạn vẫn khỏe mạnh, chỉ nhẹ hơn mức lý tưởng (%.1f kg) khoảng %.1f kg nên không cần tăng gấp.\n",
                    canNangLyTuong, soKg));
        }

        if (tinhTrang == 0) {
            loiKhuyen.append("• Thiếu cân có thể làm bạn dễ mệt và giảm sức đề kháng.\n");
        } else if (tinhTrang == 1) {
            loiKhuyen.append("• Chỉ số BMI tốt, hãy duy trì thói quen hiện tại.\n");
        } else if (chiSoBmi >= TienIch.BMI_RANH_GIOI_BEO_PHI) {
            loiKhuyen.append("• Béo phì làm tăng nguy cơ tim mạch và tiểu đường, bạn nên gặp bác sĩ để có kế hoạch phù hợp.\n");
        } else {
            loiKhuyen.append("• Bạn đang thừa cân nhẹ, điều chỉnh sớm sẽ dễ hơn nhiều.\n");
        }

        if (laThieuNien) {
            loiKhuyen.append("• Với người dưới 18 tuổi, BMI cần so với biểu đồ tăng trưởng nên kết quả chỉ mang tính tham khảo.\n");
        }

        // ----- 2. Lời khuyên vận động -----
        loiKhuyen.append("\nVẬN ĐỘNG\n");
        if (laThieuNien) {
            loiKhuyen.append("• Vận động ít nhất 60 phút mỗi ngày, ví dụ ");
            loiKhuyen.append(laNam ? "bóng đá, bóng rổ, bơi lội" : "nhảy, cầu lông, bơi lội, yoga nhẹ");
            loiKhuyen.append(".\n");
            loiKhuyen.append("• Chưa nên tập tạ nặng hay ép cân khi cơ thể đang phát triển.\n");
        } else if (laNam) {
            if (tinhTrang == 0) {
                loiKhuyen.append("• Tập gym 3–4 buổi/tuần với các bài tạ lớn (squat, đẩy ngực, kéo xà) để tăng cơ, hạn chế cardio quá nhiều.\n");
            } else if (tinhTrang == 1) {
                loiKhuyen.append("• Tập gym 3 buổi/tuần, xen kẽ 2 buổi chạy bộ hoặc đạp xe để giữ vóc dáng.\n");
            } else {
                loiKhuyen.append("• Chạy bộ, đạp xe hoặc bơi 4–5 buổi/tuần (30–45 phút) và tập tạ 2 buổi để giữ cơ khi giảm mỡ.\n");
            }
        } else {
            if (tinhTrang == 0) {
                loiKhuyen.append("• Tập tạ nhẹ và yoga/pilates 3–4 buổi/tuần để tăng cân săn chắc, hạn chế cardio quá nhiều.\n");
            } else if (tinhTrang == 1) {
                loiKhuyen.append("• Kết hợp yoga/pilates, tạ nhẹ và cardio nhẹ 3–4 buổi/tuần để cơ thể săn chắc, dẻo dai.\n");
            } else {
                loiKhuyen.append("• Đi bộ nhanh, nhảy aerobic/zumba hoặc bơi 4–5 buổi/tuần (30–45 phút), thêm 2 buổi tạ nhẹ để săn chắc.\n");
            }
        }

        if (!laThieuNien) {
            if (laLonTuoi) {
                loiKhuyen.append("• Từ 40 tuổi trở lên: khởi động kỹ 10 phút, ưu tiên bài tập ít chấn thương khớp, tránh tạ quá nặng và nên khám sức khỏe trước khi tập mạnh.\n");
            } else {
                loiKhuyen.append("• Bạn còn trẻ nên có thể tập cường độ vừa đến cao, tăng dần mức tạ hoặc quãng đường mỗi tuần.\n");
            }
        }

        // ----- 3. Lời khuyên dinh dưỡng -----
        loiKhuyen.append("\nDINH DƯỠNG\n");
        if (tinhTrang == 0) {
            loiKhuyen.append("• Ăn thêm khoảng 300–500 kcal mỗi ngày, chia 5–6 bữa nhỏ; tăng trứng, thịt, cá, sữa và tinh bột tốt (gạo lứt, khoai).\n");
        } else if (tinhTrang == 1) {
            loiKhuyen.append("• Ăn cân bằng đạm, tinh bột, chất béo tốt và rau xanh; uống đủ khoảng 2 lít nước mỗi ngày.\n");
        } else {
            loiKhuyen.append("• Giảm khoảng 300–500 kcal mỗi ngày, hạn chế đồ ngọt, nước ngọt, đồ chiên rán; tăng rau xanh và uống đủ nước.\n");
        }

        if (laThieuNien) {
            loiKhuyen.append("• Đang tuổi lớn nên không tự ý ăn kiêng; hãy ăn đủ 3 bữa chính và hỏi bác sĩ nếu cần tăng hoặc giảm cân.\n");
        } else {
            if (laNam) {
                loiKhuyen.append("• Nam giới tập tạ nên ăn đủ đạm sau buổi tập và hạn chế bia rượu.\n");
            } else {
                loiKhuyen.append("• Nữ giới nên chú ý bổ sung sắt và canxi (rau xanh đậm, đậu, sữa, cá nhỏ) và tránh ăn kiêng quá khắt khe.\n");
            }
            if (laLonTuoi) {
                loiKhuyen.append("• Từ 40 tuổi trở lên: giảm muối, hạn chế mỡ động vật, tăng chất xơ và bổ sung canxi, vitamin D.\n");
            }
        }

        loiKhuyen.append("\nLưu ý: đây chỉ là gợi ý tham khảo, không thay thế lời khuyên của bác sĩ.");
        return loiKhuyen.toString();
    }

    // ------------------------------------------------------------
    // Đổi giao diện sáng / tối
    // ------------------------------------------------------------
    private void doiGiaoDien() {
        boolean dangToi = TienIch.dangTrongCheDoToi(this);
        int cheDoMoi = dangToi ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;

        KhoDuLieu.luuCheDoGiaoDien(this, cheDoMoi);
        Toast.makeText(this,
                dangToi ? "Đã chuyển sang giao diện sáng" : "Đã chuyển sang giao diện tối",
                Toast.LENGTH_SHORT).show();

        // Dòng này làm màn hình tự tạo lại với giao diện mới
        AppCompatDelegate.setDefaultNightMode(cheDoMoi);
    }

    // Đang sáng thì hiện mặt trăng (bấm để sang tối), đang tối thì hiện mặt trời
    private void capNhatBieuTuongGiaoDien() {
        if (TienIch.dangTrongCheDoToi(this)) {
            nutDoiGiaoDien.setImageResource(R.drawable.bieu_tuong_mat_troi);
        } else {
            nutDoiGiaoDien.setImageResource(R.drawable.bieu_tuong_mat_trang);
        }
    }

    // ------------------------------------------------------------
    // Menu 3 sọc (chỉ có 2 mục)
    // ------------------------------------------------------------
    private void hienMenu(View nutBam) {
        PopupMenu menuNho = new PopupMenu(this, nutBam);
        menuNho.getMenu().add(0, MUC_LICH_SU, 0, "Lịch sử đo");
        menuNho.getMenu().add(0, MUC_DANH_GIA, 1, "Đánh giá ứng dụng");

        menuNho.setOnMenuItemClickListener(mucDuocChon -> {
            int maMuc = mucDuocChon.getItemId();
            if (maMuc == MUC_LICH_SU) {
                startActivity(new Intent(this, LichSuActivity.class));
            } else if (maMuc == MUC_DANH_GIA) {
                hienHopThoaiDanhGia();
            }
            return true;
        });
        menuNho.show();
    }

    // Hộp thoại đánh giá ứng dụng
    private void hienHopThoaiDanhGia() {
        View noiDungHopThoai = getLayoutInflater().inflate(R.layout.hop_thoai_danh_gia, null);
        EditText nhapHoTen = noiDungHopThoai.findViewById(R.id.nhap_ho_ten);
        EditText nhapDanhGia = noiDungHopThoai.findViewById(R.id.nhap_danh_gia);
        Button nutGuiDanhGia = noiDungHopThoai.findViewById(R.id.nut_gui_danh_gia);

        AlertDialog hopThoai = new AlertDialog.Builder(this).setView(noiDungHopThoai).create();
        // Nền trong suốt để thấy được thẻ bo góc bên trong
        hopThoai.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        hopThoai.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        nutGuiDanhGia.setOnClickListener(v -> {
            String hoTen = nhapHoTen.getText().toString().trim();
            String noiDungDanhGia = nhapDanhGia.getText().toString().trim();

            if (hoTen.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập họ tên của bạn", Toast.LENGTH_SHORT).show();
                return;
            }
            if (noiDungDanhGia.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Cảm ơn " + hoTen + " đã gửi đánh giá!", Toast.LENGTH_LONG).show();
            hopThoai.dismiss(); // Đóng hộp thoại, lần sau mở lại sẽ là form trống
        });

        hopThoai.show();
    }
}