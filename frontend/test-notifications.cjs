const axios = require('axios');

async function testNotifications() {
    try {
        console.log("Logging in as employee...");
        let res = await axios.post('http://localhost:8080/api/auth/login', { email: 'employee.test@hrms.local', password: 'Test@1234' });
        const empToken = res.data.token;
        const empAxios = axios.create({ headers: { Authorization: 'Bearer ' + empToken } });

        console.log("Logging in as manager...");
        res = await axios.post('http://localhost:8080/api/auth/login', { email: 'manager.test@hrms.local', password: 'Test@1234' });
        const mgrToken = res.data.token;
        const mgrAxios = axios.create({ headers: { Authorization: 'Bearer ' + mgrToken } });

        console.log("Employee submits leave...");
        let leaveReq = await empAxios.post('http://localhost:8080/api/leave/requests', {
            leaveType: { leaveTypeId: 1 },
            startDate: '2026-12-01',
            endDate: '2026-12-02',
            reason: 'Test Leave'
        });
        const leaveId = leaveReq.data.leaveRequestId;

        console.log("Checking manager notifications...");
        let notifs = await mgrAxios.get('http://localhost:8080/api/notifications');
        console.log("Manager notifications count:", notifs.data.content.length);
        const mgrNotif = notifs.data.content.find(n => n.relatedEntityId === String(leaveId));
        if (mgrNotif) {
            console.log("Success: Manager received notification for leave:", mgrNotif.title);
        } else {
            console.log("Failed: Manager did not receive notification");
        }

        console.log("Manager approves leave...");
        await mgrAxios.patch('http://localhost:8080/api/leave/requests/' + leaveId + '/review', { status: 'APPROVED' });

        console.log("Checking employee notifications...");
        notifs = await empAxios.get('http://localhost:8080/api/notifications');
        console.log("Employee notifications count:", notifs.data.content.length);
        const empNotif = notifs.data.content.find(n => n.relatedEntityId === String(leaveId));
        if (empNotif) {
            console.log("Success: Employee received notification for approval:", empNotif.title);
        } else {
            console.log("Failed: Employee did not receive notification");
        }

        console.log("All notification tests passed.");
    } catch (e) {
        console.error("Test failed", e.response?.data || e.message);
    }
}
testNotifications();
