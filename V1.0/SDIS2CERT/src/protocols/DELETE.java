package protocols;

import Utilities.PeerData;
import Utilities.RefValue;
import communication.TCP_Client;

public class DELETE extends TCP_Client{
	public DELETE(PeerData data, RefValue<Boolean> accept) {
		super(data.addr.port, data.addr.ip, accept);
	}

	@Override
	public void run(){
		super.baserun();
		if(failed_init)
			return;
	}
}
