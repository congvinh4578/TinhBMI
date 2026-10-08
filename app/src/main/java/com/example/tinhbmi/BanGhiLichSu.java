package com.example.tinhbmi;

/** Một lần đo BMI được lưu trong lịch sử. */
public class BanGhiLichSu {

    public final long thoiGian;
    public final int chieuCao;
    public final int canNang;
    public final float chiSoBmi;
    public final String phanLoai;

    public BanGhiLichSu(long thoiGian, int chieuCao, int canNang, float chiSoBmi, String phanLoai) {
        this.thoiGian = thoiGian;
        this.chieuCao = chieuCao;
        this.canNang = canNang;
        this.chiSoBmi = chiSoBmi;
        this.phanLoai = phanLoai;
    }

    /** Nội dung văn bản sẽ được gửi đi khi người dùng bấm "Chia sẻ". */
    public String taoNoiDungChiaSe() {
        return String.format(TienIch.NGON_NGU_VIET,
                "Kết quả BMI của tôi (%s)\n"
                        + "• Chiều cao: %d cm\n"
                        + "• Cân nặng: %d kg\n"
                        + "• Chỉ số BMI: %.1f (%s)\n\n"
                        + "Đo bằng ứng dụng Máy Tính BMI.",
                TienIch.dinhDangThoiGian(thoiGian), chieuCao, canNang, chiSoBmi, phanLoai);
    }
}