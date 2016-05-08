package Utilities;

import java.util.Comparator;

import communication.messages.DeleteRequestBody;
import communication.messages.MessageHeader;

public class MessageStamp implements java.io.Serializable, /*Comparator<MessageStamp>,*/Comparable<MessageStamp>{

	public MessageHeader.MessageType msg;
	public String fileid;
	public long timestamp;
	
	public MessageStamp(){}
	
	public MessageStamp(MessageHeader msgHeader)
	{
		msg = msgHeader.getMessageType();
		fileid = msgHeader.getFileId();
		timestamp = msgHeader.getTimeStamp();
	}
	
	public MessageStamp(MessageHeader msgHeader,DeleteRequestBody del)
	{
		msg = msgHeader.getMessageType();
		fileid = del.FileID;
		timestamp = msgHeader.getTimeStamp();
	}
	
	public void CopyFrom(MessageStamp ms)
	{
		msg = ms.msg;
		fileid = ms.fileid;
		timestamp = ms.timestamp;
	}

	/*@Override
	public int compare(MessageStamp o1, MessageStamp o2) {
		long r = o1.timestamp-o2.timestamp;
		if (r>0) return 1;
		if (r<0) return -1;
		return 0;
	}*/

	@Override
	public int compareTo(MessageStamp o) {
		long r = this.timestamp-o.timestamp;
		if (r>0) return 1;
		if (r<0) return -1;
		return 0;
	}
}
