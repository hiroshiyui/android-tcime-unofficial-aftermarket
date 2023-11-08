# android-tcime-unofficial-aftermarket

## 「注音倉頡輸入法非官方版」之「副廠料件版」

這是「注音倉頡輸入法非官方版」（[原始網站](https://code.google.com/archive/p/android-tcime-unofficial/)、[原非官方版作者 GitHub 映射站](https://github.com/scribetw/android-tcime-unofficial)）的「副廠料件版」(aftermarket)。我只是接受個案付費委託，做一些必要的新版 Android 相容維護，恕不接受增加新功能的請求。

## 本「副廠料件版」之任何維護委託，均為有償服務

委託請詳述需求與願意支付之金額。

## 「可是『注音倉頡輸入法』、『注音倉頡輸入法非官方版』都沒有收費！」

[授權條款](./COPYING)並無禁止我為維護委託收取合理報償。他人有能力無償付出、貢獻社會，我尊敬，但是我不一樣，我需要錢吃飯。

## 「我之前付過一次錢了，為何這次又要收我錢？」

除非此次修改是因為之前我維護過的地方沒有寫好，得因保固原則負責維修，否則皆應按件計酬。

## 「為何拒絕『加功能』、『改功能』？」

這隻程式的寫法已經與現今 Android [InputMethodService](https://developer.android.com/reference/android/inputmethodservice/InputMethodService) 運作方式出入甚大了，現在做 Android 輸入法，連按鍵都要自己刻，沒有 [Key](https://developer.android.com/reference/kotlin/android/inputmethodservice/Keyboard.Key) 這種現成元件，其餘改動更是多如牛毛。Android 內部若已完全禁用或不再相容舊版寫法，除非有人願意協助大幅改寫這隻程式，使其契合最新的 InputMethodService，不然有些需要大破大立才得以實作的功能，我也愛莫能助。
