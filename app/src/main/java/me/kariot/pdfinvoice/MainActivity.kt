package me.kariot.pdfinvoice

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.itextpdf.text.*
import com.itextpdf.text.PageSize.A4
import com.itextpdf.text.pdf.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    val colorPrimary = BaseColor(40, 116, 240)
    val FONT_SIZE_DEFAULT = 12f
    val FONT_SIZE_SMALL = 8f
    var basfontLight: BaseFont =
        BaseFont.createFont("assets/fonts/app_font_light.ttf", "UTF-8", BaseFont.EMBEDDED)
    var appFontLight = Font(basfontLight, FONT_SIZE_SMALL)

    var basfontRegular: BaseFont =
        BaseFont.createFont("assets/fonts/app_font_regular.ttf", "UTF-8", BaseFont.EMBEDDED)
    var appFontRegular = Font(basfontRegular, FONT_SIZE_DEFAULT)


    var basfontSemiBold: BaseFont =
        BaseFont.createFont("assets/fonts/app_font_semi_bold.ttf", "UTF-8", BaseFont.EMBEDDED)
    var appFontSemiBold = Font(basfontSemiBold, 24f)


    var basfontBold: BaseFont =
        BaseFont.createFont("assets/fonts/app_font_bold.ttf", "UTF-8", BaseFont.EMBEDDED)
    var appFontBold = Font(basfontBold, FONT_SIZE_DEFAULT)

    val PADDING_EDGE = 40f
    val TEXT_TOP_PADDING = 3f
    val TABLE_TOP_PADDING = 10f
    val TEXT_TOP_PADDING_EXTRA = 30f
    val BILL_DETAILS_TOP_PADDING = 80f
    val data = ArrayList<ModelItems>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initData()
        generatePdf.setOnClickListener {
            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                        if (report.areAllPermissionsGranted()) {

                            appFontRegular.color = BaseColor.WHITE
                            appFontRegular.size = 10f
                            val doc = Document(A4, 0f, 0f, 0f, 0f)
                            val outPath =
                                getExternalFilesDir(null).toString() + "/my_invoice.pdf" //location where the pdf will store
                            Log.d("loc", outPath)
                            val writer = PdfWriter.getInstance(doc, FileOutputStream(outPath))
                            doc.open()
                            //Header Column Init with width nad no. of columns
                            initInvoiceHeader(doc)
                            doc.setMargins(0f, 0f, PADDING_EDGE, PADDING_EDGE)
                            initBillDetails(doc)
                            addLine(writer)
                            initTableHeader(doc)
                            initItemsTable(doc)
                            initPriceDetails(doc)
                            initFooter(doc)
                            doc.close()


                            val file = File(outPath)
                            val path: Uri = FileProvider.getUriForFile(
                                applicationContext,
                                BuildConfig.APPLICATION_ID + ".provider",
                                file
                            )
                            try {
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.setDataAndType(path, "application/pdf")
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                toast("There is no PDF Viewer ")
                            }


                        } else {
                            toast("permissions missing :(")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest>,
                        token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                }).check()
        }

    }

    private fun initFooter(doc: Document) {
        appFontRegular.color = colorPrimary
        val footerTable = PdfPTable(1)
        footerTable.totalWidth = A4.width
        footerTable.isLockedWidth = true
        val thankYouCell =
            PdfPCell(Phrase("THANK YOU FOR YOUR BUSINESS", appFontRegular))
        thankYouCell.border = Rectangle.NO_BORDER
        thankYouCell.paddingLeft = PADDING_EDGE
        thankYouCell.paddingTop = PADDING_EDGE
        thankYouCell.horizontalAlignment = Rectangle.ALIGN_CENTER
        footerTable.addCell(thankYouCell)
        doc.add(footerTable)

    }

    private fun initData() {
        for (i in 1..15) {
            data.add(
                ModelItems(
                    "Item $i",
                    "Description $i",
                    (1..1000).random(),
                    (1234..123456).random(),
                    (1..99).random(),
                    (1234..132456).random()
                )
            )
        }
    }

    private fun initPriceDetails(doc: Document) {
        val priceDetailsTable = PdfPTable(2)
        priceDetailsTable.totalWidth = A4.width
        priceDetailsTable.setWidths(floatArrayOf(5f, 2f))
        priceDetailsTable.isLockedWidth = true

        appFontRegular.color = colorPrimary
        val txtSubTotalCell = PdfPCell(Phrase("Sub Total : ", appFontRegular))
        txtSubTotalCell.border = Rectangle.NO_BORDER
        txtSubTotalCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        txtSubTotalCell.paddingTop = TEXT_TOP_PADDING_EXTRA
        priceDetailsTable.addCell(txtSubTotalCell)
        appFontBold.color = BaseColor.BLACK
        val totalPriceCell = PdfPCell(Phrase("AED 12000", appFontBold))
        totalPriceCell.border = Rectangle.NO_BORDER
        totalPriceCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        totalPriceCell.paddingTop = TEXT_TOP_PADDING_EXTRA
        totalPriceCell.paddingRight = PADDING_EDGE
        priceDetailsTable.addCell(totalPriceCell)


        val txtTaxCell = PdfPCell(Phrase("Tax Total : ", appFontRegular))
        txtTaxCell.border = Rectangle.NO_BORDER
        txtTaxCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        txtTaxCell.paddingTop = TEXT_TOP_PADDING
        priceDetailsTable.addCell(txtTaxCell)

        val totalTaxCell = PdfPCell(Phrase("AED 100", appFontBold))
        totalTaxCell.border = Rectangle.NO_BORDER
        totalTaxCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        totalTaxCell.paddingTop = TEXT_TOP_PADDING
        totalTaxCell.paddingRight = PADDING_EDGE
        priceDetailsTable.addCell(totalTaxCell)

        val txtTotalCell = PdfPCell(Phrase("TOTAL : ", appFontRegular))
        txtTotalCell.border = Rectangle.NO_BORDER
        txtTotalCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        txtTotalCell.paddingTop = TEXT_TOP_PADDING
        txtTotalCell.paddingBottom = TEXT_TOP_PADDING
        txtTotalCell.paddingLeft = PADDING_EDGE
        priceDetailsTable.addCell(txtTotalCell)
        appFontBold.color = colorPrimary
        val totalCell = PdfPCell(Phrase("AED 12100", appFontBold))
        totalCell.border = Rectangle.NO_BORDER
        totalCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        totalCell.paddingTop = TEXT_TOP_PADDING
        totalCell.paddingBottom = TEXT_TOP_PADDING
        totalCell.paddingRight = PADDING_EDGE
        priceDetailsTable.addCell(totalCell)

        doc.add(priceDetailsTable)
    }

    private fun initItemsTable(doc: Document) {
        val itemsTable = PdfPTable(5)
        itemsTable.isLockedWidth = true
        itemsTable.totalWidth = A4.width
        itemsTable.setWidths(floatArrayOf(1.5f, 1f, 1f, .6f, 1.1f))

        for (item in data) {
            itemsTable.deleteBodyRows()

            val itemdetails = PdfPTable(1)
            val itemName = PdfPCell(Phrase(item.itemName, appFontRegular))
            itemName.border = Rectangle.NO_BORDER
            val itemDesc = PdfPCell(Phrase(item.itemDesc, appFontLight))
            itemDesc.border = Rectangle.NO_BORDER
            itemdetails.addCell(itemName)
            itemdetails.addCell(itemDesc)
            val itemCell = PdfPCell(itemdetails)
            itemCell.border = Rectangle.NO_BORDER
            itemCell.paddingTop = TABLE_TOP_PADDING
            itemCell.paddingLeft = PADDING_EDGE
            itemsTable.addCell(itemCell)


            val quantityCell = PdfPCell(Phrase(item.quantity.toString(), appFontRegular))
            quantityCell.border = Rectangle.NO_BORDER
            quantityCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
            quantityCell.paddingTop = TABLE_TOP_PADDING
            itemsTable.addCell(quantityCell)

            val disAmount = PdfPCell(Phrase("AED ${item.disAmount}", appFontRegular))
            disAmount.border = Rectangle.NO_BORDER
            disAmount.horizontalAlignment = Rectangle.ALIGN_RIGHT
            disAmount.paddingTop = TABLE_TOP_PADDING
            itemsTable.addCell(disAmount)

            val vat = PdfPCell(Phrase(item.vat.toString(), appFontRegular))
            vat.border = Rectangle.NO_BORDER
            vat.horizontalAlignment = Rectangle.ALIGN_RIGHT
            vat.paddingTop = TABLE_TOP_PADDING
            itemsTable.addCell(vat)

            val netAmount = PdfPCell(Phrase("AED ${item.netAmount}", appFontRegular))
            netAmount.horizontalAlignment = Rectangle.ALIGN_RIGHT
            netAmount.border = Rectangle.NO_BORDER
            netAmount.paddingTop = TABLE_TOP_PADDING
            netAmount.paddingRight = PADDING_EDGE
            itemsTable.addCell(netAmount)
            doc.add(itemsTable)
        }
    }

    private fun initTableHeader(doc: Document) {

        doc.add(Paragraph("\n\n\n\n\n")) //adds blank line to place table header below the line

        val titleTable = PdfPTable(5)
        titleTable.isLockedWidth = true
        titleTable.totalWidth = A4.width
        titleTable.setWidths(floatArrayOf(1.5f, 1f, 1f, .6f, 1.1f))
        appFontBold.color = colorPrimary

        val itemCell = PdfPCell(Phrase("Description", appFontBold))
        itemCell.border = Rectangle.NO_BORDER
        itemCell.paddingTop = TABLE_TOP_PADDING
        itemCell.paddingBottom = TABLE_TOP_PADDING
        itemCell.paddingLeft = PADDING_EDGE
        titleTable.addCell(itemCell)


        val quantityCell = PdfPCell(Phrase("Quantity", appFontBold))
        quantityCell.border = Rectangle.NO_BORDER
        quantityCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        quantityCell.paddingBottom = TABLE_TOP_PADDING
        quantityCell.paddingTop = TABLE_TOP_PADDING
        titleTable.addCell(quantityCell)

        val disAmount = PdfPCell(Phrase("DIS. Amount", appFontBold))
        disAmount.border = Rectangle.NO_BORDER
        disAmount.horizontalAlignment = Rectangle.ALIGN_RIGHT
        disAmount.paddingBottom = TABLE_TOP_PADDING
        disAmount.paddingTop = TABLE_TOP_PADDING
        titleTable.addCell(disAmount)

        val vat = PdfPCell(Phrase("VAT %", appFontBold))
        vat.border = Rectangle.NO_BORDER
        vat.horizontalAlignment = Rectangle.ALIGN_RIGHT
        vat.paddingBottom = TABLE_TOP_PADDING
        vat.paddingTop = TABLE_TOP_PADDING
        titleTable.addCell(vat)

        val netAmount = PdfPCell(Phrase("Net Amount", appFontBold))
        netAmount.horizontalAlignment = Rectangle.ALIGN_RIGHT
        netAmount.border = Rectangle.NO_BORDER
        netAmount.paddingTop = TABLE_TOP_PADDING
        netAmount.paddingBottom = TABLE_TOP_PADDING
        netAmount.paddingRight = PADDING_EDGE
        titleTable.addCell(netAmount)
        doc.add(titleTable)
    }

    private fun addLine(writer: PdfWriter) {
        val canvas: PdfContentByte = writer.directContent
        canvas.setColorStroke(colorPrimary)
        canvas.moveTo(40.0, 480.0)

        // Drawing the line
        canvas.lineTo(560.0, 480.0)
        canvas.setLineWidth(3f)

        // Closing the path stroke
        canvas.closePathStroke()
    }

    private fun initBillDetails(doc: Document) {
        val billDetailsTable =
            PdfPTable(3)  // table to show customer address, invoice, date and total amount
        billDetailsTable.setWidths(
            floatArrayOf(
                2f,
                1.82f,
                2f
            )
        )
        billDetailsTable.isLockedWidth = true
        billDetailsTable.paddingTop = 30f

        billDetailsTable.totalWidth =
            A4.width // set content width to fill document
        val customerAddressTable = PdfPTable(1)
        appFontRegular.color = BaseColor.GRAY
        appFontRegular.size = 8f
        val txtBilledToCell = PdfPCell(
            Phrase(
                "Billed To",
                appFontLight
            )
        )
        txtBilledToCell.border = Rectangle.NO_BORDER
        customerAddressTable.addCell(
            txtBilledToCell
        )
        appFontRegular.size = FONT_SIZE_DEFAULT
        appFontRegular.color = BaseColor.BLACK
        val clientAddressCell1 = PdfPCell(
            Paragraph(
                "Sreehari K",
                appFontRegular
            )
        )
        clientAddressCell1.border = Rectangle.NO_BORDER
        clientAddressCell1.paddingTop = TEXT_TOP_PADDING
        customerAddressTable.addCell(clientAddressCell1)

        val clientAddressCell2 = PdfPCell(
            Paragraph(
                "Address Line 1",
                appFontRegular
            )
        )
        clientAddressCell2.border = Rectangle.NO_BORDER
        clientAddressCell2.paddingTop = TEXT_TOP_PADDING
        customerAddressTable.addCell(clientAddressCell2)


        val clientAddressCell3 = PdfPCell(
            Paragraph(
                "Address Line 2",
                appFontRegular
            )
        )
        clientAddressCell3.border = Rectangle.NO_BORDER
        clientAddressCell3.paddingTop = TEXT_TOP_PADDING
        customerAddressTable.addCell(clientAddressCell3)


        val clientAddressCell4 = PdfPCell(
            Paragraph(
                "Address Line 3",
                appFontRegular
            )
        )
        clientAddressCell4.border = Rectangle.NO_BORDER
        clientAddressCell4.paddingTop = TEXT_TOP_PADDING
        customerAddressTable.addCell(clientAddressCell4)

        val billDetailsCell1 = PdfPCell(customerAddressTable)
        billDetailsCell1.border = Rectangle.NO_BORDER

        billDetailsCell1.paddingTop = BILL_DETAILS_TOP_PADDING

        billDetailsCell1.paddingLeft = PADDING_EDGE

        billDetailsTable.addCell(billDetailsCell1)


        val invoiceNumAndData = PdfPTable(1)
        appFontRegular.color = BaseColor.LIGHT_GRAY
        appFontRegular.size = 8f
        val txtInvoiceNumber = PdfPCell(Phrase("Invoice Number", appFontLight))
        txtInvoiceNumber.paddingTop = BILL_DETAILS_TOP_PADDING
        txtInvoiceNumber.border = Rectangle.NO_BORDER
        invoiceNumAndData.addCell(txtInvoiceNumber)
        appFontRegular.color = BaseColor.BLACK
        appFontRegular.size = 12f
        val invoiceNumber = PdfPCell(Phrase("BMC00${(1234..9879).random()}", appFontRegular))
        invoiceNumber.border = Rectangle.NO_BORDER
        invoiceNumber.paddingTop = TEXT_TOP_PADDING
        invoiceNumAndData.addCell(invoiceNumber)

        appFontRegular.color = BaseColor.LIGHT_GRAY
        appFontRegular.size = 5f
        val txtDate = PdfPCell(Phrase("Date Of Issue", appFontLight))
        txtDate.paddingTop = TEXT_TOP_PADDING_EXTRA
        txtDate.border = Rectangle.NO_BORDER
        invoiceNumAndData.addCell(txtDate)

        appFontRegular.color = BaseColor.BLACK
        appFontRegular.size = FONT_SIZE_DEFAULT
        val dateCell = PdfPCell(Phrase("04/11/2019", appFontRegular))
        dateCell.border = Rectangle.NO_BORDER
        invoiceNumAndData.addCell(dateCell)

        val dataInvoiceNumAndData = PdfPCell(invoiceNumAndData)
        dataInvoiceNumAndData.border = Rectangle.NO_BORDER
        billDetailsTable.addCell(dataInvoiceNumAndData)

        val totalPriceTable = PdfPTable(1)
        val txtInvoiceTotal = PdfPCell(Phrase("Invoice Total", appFontLight))
        txtInvoiceTotal.paddingTop = BILL_DETAILS_TOP_PADDING
        txtInvoiceTotal.horizontalAlignment = Rectangle.ALIGN_RIGHT
        txtInvoiceTotal.border = Rectangle.NO_BORDER
        totalPriceTable.addCell(txtInvoiceTotal)

        appFontSemiBold.color = colorPrimary
        val totalAomountCell = PdfPCell(Phrase("AED ${(111..21398).random()}", appFontSemiBold))
        totalAomountCell.border = Rectangle.NO_BORDER
        totalAomountCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        totalPriceTable.addCell(totalAomountCell)
        val dataTotalAmount = PdfPCell(totalPriceTable)
        dataTotalAmount.border = Rectangle.NO_BORDER
        dataTotalAmount.paddingRight = PADDING_EDGE
        dataTotalAmount.verticalAlignment = Rectangle.ALIGN_BOTTOM

        billDetailsTable.addCell(dataTotalAmount)
        doc.add(billDetailsTable)
    }

    private fun initInvoiceHeader(doc: Document) {
        val d = resources.getDrawable(R.drawable.gear)
        val bitDw = d as BitmapDrawable
        val bmp = bitDw.bitmap
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val image = Image.getInstance(stream.toByteArray())
        val headerTable = PdfPTable(3)
        headerTable.setWidths(
            floatArrayOf(
                1.3f,
                1f,
                1f
            )
        ) // adds 3 colomn horizontally
        headerTable.isLockedWidth = true
        headerTable.totalWidth = A4.width // set content width to fill document
        val cell = PdfPCell(Image.getInstance(image)) // Logo Cell
        cell.border = Rectangle.NO_BORDER // Removes border
        cell.paddingTop = TEXT_TOP_PADDING_EXTRA // sets padding
        cell.paddingRight = TABLE_TOP_PADDING
        cell.paddingLeft = PADDING_EDGE
        cell.horizontalAlignment = Rectangle.ALIGN_LEFT
        cell.paddingBottom = TEXT_TOP_PADDING_EXTRA

        cell.backgroundColor = colorPrimary // sets background color
        cell.horizontalAlignment = Element.ALIGN_CENTER
        headerTable.addCell(cell) // Adds first cell with logo

        val contactTable =
            PdfPTable(1) // new vertical table for contact details
        val phoneCell =
            PdfPCell(
                Paragraph(
                    "+91 8547984369",
                    appFontRegular
                )
            )
        phoneCell.border = Rectangle.NO_BORDER
        phoneCell.horizontalAlignment = Element.ALIGN_RIGHT
        phoneCell.paddingTop = TEXT_TOP_PADDING

        contactTable.addCell(phoneCell)

        val emailCellCell = PdfPCell(Phrase("sreeharikariot@gmail.com", appFontRegular))
        emailCellCell.border = Rectangle.NO_BORDER
        emailCellCell.horizontalAlignment = Element.ALIGN_RIGHT
        emailCellCell.paddingTop = TEXT_TOP_PADDING

        contactTable.addCell(emailCellCell)

        val webCell = PdfPCell(Phrase("www.kariot.me", appFontRegular))
        webCell.border = Rectangle.NO_BORDER
        webCell.paddingTop = TEXT_TOP_PADDING
        webCell.horizontalAlignment = Element.ALIGN_RIGHT

        contactTable.addCell(webCell)


        val headCell = PdfPCell(contactTable)
        headCell.border = Rectangle.NO_BORDER
        headCell.horizontalAlignment = Element.ALIGN_RIGHT
        headCell.verticalAlignment = Element.ALIGN_MIDDLE
        headCell.backgroundColor = colorPrimary
        headerTable.addCell(headCell)

        val address = PdfPTable(1)
        val line1 = PdfPCell(
            Paragraph(
                "Address Line 1",
                appFontRegular
            )
        )
        line1.border = Rectangle.NO_BORDER
        line1.paddingTop = TEXT_TOP_PADDING
        line1.horizontalAlignment = Element.ALIGN_RIGHT

        address.addCell(line1)

        val line2 = PdfPCell(Paragraph("Address Line 2", appFontRegular))
        line2.border = Rectangle.NO_BORDER
        line2.paddingTop = TEXT_TOP_PADDING
        line2.horizontalAlignment = Element.ALIGN_RIGHT

        address.addCell(line2)

        val line3 = PdfPCell(Paragraph("Address Line 3", appFontRegular))
        line3.border = Rectangle.NO_BORDER
        line3.paddingTop = TEXT_TOP_PADDING
        line3.horizontalAlignment = Element.ALIGN_RIGHT

        address.addCell(line3)


        val addressHeadCell = PdfPCell(address)
        addressHeadCell.border = Rectangle.NO_BORDER
        addressHeadCell.setLeading(22f, 25f)
        addressHeadCell.horizontalAlignment = Element.ALIGN_RIGHT
        addressHeadCell.verticalAlignment = Element.ALIGN_MIDDLE
        addressHeadCell.backgroundColor = colorPrimary
        addressHeadCell.paddingRight = PADDING_EDGE
        headerTable.addCell(addressHeadCell)

        doc.add(headerTable)
    }

}