<%@ include file="header.jspf" %>
<a class="back-link" href="${pageContext.request.contextPath}${adminView ? '/admin/inquiries' : '/account/inquiries'}">← 문의 목록</a>
<div class="support-heading"><div><p class="eyebrow">INQUIRY #${inquiry.id}</p><h1 class="inquiry-title"><c:out value="${inquiry.title}"/></h1></div><span class="status-pill ${inquiry.status == 'ANSWERED' ? 'answered' : ''}">${inquiry.status == 'ANSWERED' ? '답변 완료' : '답변 대기'}</span></div>
<div class="support-layout">
<section class="support-panel"><h2>문의 내용</h2><div class="muted"><fmt:formatDate value="${inquiry.createdAt}" pattern="yyyy.MM.dd HH:mm"/> · ${inquiry.category == 'PRODUCT' ? '상품 정보' : inquiry.category == 'ORDER' ? '주문·배송' : inquiry.category == 'RETURN' ? '교환·반품' : '기타'}</div>
<p class="context-line"><c:out value="${inquiry.productName}"/> <c:out value="${inquiry.orderNumber}"/></p><div class="preserve"><c:out value="${inquiry.body}"/></div>
<hr><h2>담당자 답변</h2><c:choose><c:when test="${inquiry.status == 'ANSWERED'}"><div class="preserve"><c:out value="${inquiry.answer}"/></div><p class="help"><fmt:formatDate value="${inquiry.answeredAt}" pattern="yyyy.MM.dd HH:mm"/>에 답변했습니다.</p></c:when><c:otherwise><p class="muted">문의가 접수되었습니다. 담당자가 확인하고 있습니다.</p></c:otherwise></c:choose></section>
<c:if test="${adminView}">
<section class="support-panel"><div class="panel-heading"><h2>답변 작성</h2><span class="status-pill">담당자 승인 필수</span></div>
<c:if test="${inquiry.status == 'OPEN'}">
<div class="ai-status" role="status"><c:choose>
<c:when test="${inquiry.aiState == 'RUNNING' or inquiry.aiState == 'PENDING'}">AI 초안을 준비하고 있습니다. 잠시 후 <a href="${pageContext.request.contextPath}/admin/inquiry?id=${inquiry.id}">새로고침</a>해 주세요. 작성 중인 답변은 먼저 복사해 주세요.</c:when>
<c:when test="${inquiry.aiState == 'READY'}">AI 초안이 준비되었습니다. 아래 생성 이력에서 근거를 확인해 주세요.</c:when>
<c:when test="${inquiry.aiState == 'FAILED'}">AI 초안 생성에 실패했습니다. 재시도하거나 직접 답변할 수 있습니다.</c:when>
<c:otherwise>직접 답변을 작성하거나 AI 초안을 생성할 수 있습니다.</c:otherwise></c:choose></div>
<c:if test="${inquiry.needsReview and not empty inquiry.reviewReason}"><p class="review-warning"><c:out value="${inquiry.reviewReason}"/></p></c:if>
<form method="post" action="${pageContext.request.contextPath}/admin/inquiry" data-submit-lock><input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${inquiry.id}"><input type="hidden" name="action" value="generate"><button type="submit" class="support-button secondary" ${not aiEnabled ? 'disabled' : ''}>AI 초안 생성 / 재시도</button></form>
<c:if test="${not aiEnabled}"><p class="help">AI 연결 전입니다. 수동 답변은 바로 사용할 수 있습니다.</p></c:if>
</c:if>
<form method="post" class="support-form" action="${pageContext.request.contextPath}/admin/inquiry" data-submit-lock>
<input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${inquiry.id}"><input type="hidden" name="action" value="publish"><input type="hidden" name="version" value="${inquiry.version}">
<label for="answer">고객에게 보낼 답변</label><textarea id="answer" name="answer" rows="12" maxlength="5000" required><c:out value="${answerText}"/></textarea>
<p class="help">AI 초안에는 오류가 있을 수 있습니다. 상품 정보와 정책을 확인하고 등록해 주세요.</p>
<c:choose><c:when test="${conflict}"><a class="support-button secondary" href="${pageContext.request.contextPath}/admin/inquiry?id=${inquiry.id}">최신 내용 다시 불러오기</a></c:when><c:otherwise><button class="support-button" type="submit">${inquiry.status == 'ANSWERED' ? '답변 수정하여 공개' : '답변 승인하고 공개'} →</button></c:otherwise></c:choose>
</form></section>
</c:if>
</div>
<c:if test="${adminView}">
<section class="support-panel audit-panel"><h2>AI 생성 이력과 참고 자료</h2><p class="help">생성 당시 자료와 초안을 보관합니다. 실제 인용한 근거 ID와 전달한 자료를 구분해 확인하세요. 고객에게는 표시되지 않습니다.</p>
<c:if test="${empty runs}"><p class="muted">아직 생성 이력이 없습니다.</p></c:if>
<c:forEach var="run" items="${runs}"><details class="audit-run"><summary><fmt:formatDate value="${run.createdAt}" pattern="MM.dd HH:mm:ss"/> · <c:out value="${run.state}"/> · <c:out value="${run.model}"/> · ${run.elapsedMs}ms</summary>
<p class="help">입력 ${run.inputTokens} / 출력 ${run.outputTokens} 토큰 · <c:out value="${run.promptVersion}"/></p>
<p><c:out value="${run.reason}"/></p><h3>생성된 초안</h3><div class="preserve"><c:out value="${run.draft}"/></div>
<h3>인용한 근거 ID</h3><pre><c:out value="${run.citations}"/></pre><h3>전달한 자료 스냅샷</h3><pre data-json-pretty><c:out value="${run.sources}"/></pre></details></c:forEach>
</section></c:if></main></body></html>
