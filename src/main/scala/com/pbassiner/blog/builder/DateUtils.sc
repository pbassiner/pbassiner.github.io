import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

private[this] val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
private[this] val yearDateFormatter = new SimpleDateFormat("yyyy")
private[this] val postIndexDateFormatter = new SimpleDateFormat("MMMM dd, yyyy")

val currentDate = dateFormatter.format(Calendar.getInstance().getTime())
val currentYear = yearDateFormatter.format(Calendar.getInstance().getTime())

def toTimestamp(date: String): Date =
  dateFormatter.parse(date)
def toYear(date: String): String =
  yearDateFormatter.format(dateFormatter.parse(date))
def toPostedDate(date: String): String =
  postIndexDateFormatter.format(dateFormatter.parse(date))
