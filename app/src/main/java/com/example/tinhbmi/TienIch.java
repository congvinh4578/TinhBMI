package com.example.tinhbmi;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Các hàm dùng chung cho nhiều màn hình. */
public final class TienIch {

    public static final Locale NGON_NGU_VIET = Locale.forLanguageTag("vi-VN");

    public static final float BMI_BINH_THUONG_THAP = 18.5f;
    public static final float BMI_BINH_THUONG_CAO = 24.9f;
    public static final float BMI_RANH_GIOI_THUA_CAN = 25f;
    public static final float BMI_RANH_GIOI_BEO_PHI = 30f;

    private TienIch() {
    }

    // ------------------------------------------------------------
    // BMI
    // ------------------------------------------------------------
    public static float tinhChiSoBmi(int canNang, int chieuCao) {
        float chieuCaoMet = chieuCao / 100f;
        float bmiChinhXac = canNang / (chieuCaoMet * chieuCaoMet);
        return Math.round(bmiChinhXac * 10f) / 10f;
    }

    public static String layPhanLoai(float chiSoBmi) {
        if (chiSoBmi < BMI_BINH_THUONG_THAP) {
            return "Gầy";
        } else if (chiSoBmi < BMI_RANH_GIOI_THUA_CAN) {
            return "Bình thường";
        } else if (chiSoBmi < BMI_RANH_GIOI_BEO_PHI) {
            return "Thừa cân";
        } else {
            return "Béo phì";
        }
    }

    public static int layMauSac(Context nguCanh, float chiSoBmi) {
        int maMau;
        if (chiSoBmi < BMI_BINH_THUONG_THAP) {
            maMau = R.color.mau_gay;
        } else if (chiSoBmi < BMI_RANH_GIOI_THUA_CAN) {
            maMau = R.color.mau_binh_thuong;
        } else if (chiSoBmi < BMI_RANH_GIOI_BEO_PHI) {
            maMau = R.color.mau_thua_can;
        } else {
            maMau = R.color.mau_beo_phi;
        }
        return ContextCompat.getColor(nguCanh, maMau);
    }

    /** Màu chữ đặt trên nhãn phân loại (nền vàng cần chữ tối để dễ đọc). */
    public static int layMauChuTrenNhan(Context nguCanh, float chiSoBmi) {
        int maMau = chiSoBmi < BMI_BINH_THUONG_THAP ? R.color.chu_tren_nen_vang : R.color.trang;
        return ContextCompat.getColor(nguCanh, maMau);
    }

    // ------------------------------------------------------------
    // Định dạng, đo đạc
    // ------------------------------------------------------------
    public static String dinhDangThoiGian(long thoiGian) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", NGON_NGU_VIET).format(new Date(thoiGian));
    }

    public static int doiDpSangPx(Context nguCanh, int giaTriDp) {
        return Math.round(giaTriDp * nguCanh.getResources().getDisplayMetrics().density);
    }

    public static boolean dangTrongCheDoToi(Context nguCanh) {
        int cheDoHienTai = nguCanh.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return cheDoHienTai == Configuration.UI_MODE_NIGHT_YES;
    }

    // ------------------------------------------------------------
    // Giao diện toàn màn hình
    // ------------------------------------------------------------
    /**
     * Cho phần tiêu đề tràn lên thanh trạng thái, chừa chỗ cho thanh điều hướng và bàn phím.
     *
     * @param viewAnKhiCoBanPhim view sẽ được ẩn khi bàn phím mở (có thể là null)
     */
    public static void thietLapToanManHinh(Activity hoatDong, View boCucGoc,
                                           View khuVucTieuDe, View viewAnKhiCoBanPhim) {
        WindowCompat.setDecorFitsSystemWindows(hoatDong.getWindow(), false);
        hoatDong.getWindow().setStatusBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat boDieuKhien = WindowCompat.getInsetsController(
                hoatDong.getWindow(), hoatDong.getWindow().getDecorView());
        // Tiêu đề luôn nền xanh đậm nên biểu tượng thanh trạng thái luôn màu sáng
        boDieuKhien.setAppearanceLightStatusBars(false);
        boDieuKhien.setAppearanceLightNavigationBars(!dangTrongCheDoToi(hoatDong));

        final int leTrai = khuVucTieuDe.getPaddingStart();
        final int leTrenGoc = khuVucTieuDe.getPaddingTop();
        final int lePhai = khuVucTieuDe.getPaddingEnd();
        final int leDuoi = khuVucTieuDe.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(boCucGoc, (view, cuaSoChen) -> {
            Insets phanChen = cuaSoChen.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            boolean banPhimDangMo = cuaSoChen.isVisible(WindowInsetsCompat.Type.ime());

            khuVucTieuDe.setPaddingRelative(leTrai, leTrenGoc + phanChen.top, lePhai, leDuoi);
            view.setPadding(0, 0, 0, phanChen.bottom);

            if (viewAnKhiCoBanPhim != null) {
                viewAnKhiCoBanPhim.setVisibility(banPhimDangMo ? View.GONE : View.VISIBLE);
            }
            return WindowInsetsCompat.CONSUMED;
        });
    }
}