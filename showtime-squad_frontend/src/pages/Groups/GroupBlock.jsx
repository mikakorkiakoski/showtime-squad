import { getRequest, postRequest } from "../../utils/GenericHTTPMethods"
import { useUser } from '../../context/UserContext';

import React, { useRef } from 'react';

import GroupView from './GroupView'
import FunctionButton from '../../components/atoms/FunctionButton'

import './GroupBlock.scss'

const apiUrl = import.meta.env.VITE_REACT_APP_BACKEND_BASE_URL

function GroupBlock({ name = "", showSignal, groupSignal }) {
    if (showSignal == null || groupSignal == null) {
        throw new Error("Specified (individual) signals are mandatory")
    }

    const { username } = useUser()

    const epicElementRef = useRef(null);

    async function handleShow() {
        showSignal.value = !showSignal.value
        if (showSignal.value === false) {
            setTimeout(() => {
                const elementHeight = epicElementRef.current.clientHeight;

                window.scroll({
                    top: window.scrollY - elementHeight,
                    behavior: 'smooth',
                });
            }, 5);
            //   TODO: this timer is kinda scuffed
            // we have to wait otherwise it will just scroll based on old height 
            return
        }

        if (epicElementRef.current) {
            epicElementRef.current.scrollIntoView({ behavior: 'smooth' });
        }

        const group = await fetchGroupInfo(name, username)
        if (!group) {
            console.error("Could not fetch group")
            return;
        }

        groupSignal.value = group
    }

    return (
        <>
            <section className={`group-block ${showSignal.value ? 'show' : 'hide'}`} ref={epicElementRef}>
                <div className="group-card">
                    <h3 className="group-name">{name}</h3>
                    <FunctionButton onClick={handleShow} text={showSignal.value ? 'hide' : 'show'} />
                </div>

                {showSignal.value ?
                    <GroupView
                        group={
                            groupSignal.value ? groupSignal.value : { groupname: name }
                        }
                        username={username}
                    />
                    : <></>}
            </section>
        </>
    )
}

async function fetchGroupInfo(name, username) {
    try {
        const response = await getRequest({ url: `${apiUrl}/api/group/${name}` })
        if (response && response.groupname) {
            // response seems valid
            if (username && response.owner && username == response.owner) {
                //is owner, get join requests
                const joiners = await postRequest({
                    url: `${apiUrl}/api/group/requests`,
                    body: { groupname: response.groupname }
                });

                if (joiners) {
                    response.joinRequests = joiners.joinRequests;
                } else {
                    throw new Error("Could not fetch joiners")
                }
            }

            if (response.news && response.news.length > 0) {
                // fetch news
            }

            return response;
        }

        return null;
    } catch (error) {
        console.error("Error fetching groups:", error);
        // Handle error if fetchGroups fails
        return null;
    }
}

export default GroupBlock